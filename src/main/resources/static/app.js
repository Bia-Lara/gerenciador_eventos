const API_BASE = "/api/v1";
const TOKEN_KEY = "encontro.jwt";
const NAME_KEY = "encontro.name";
const CATEGORY_REGISTRATION_ERROR = "Não é possível remover esta categoria porque há usuários inscritos.";

const authView = document.querySelector("#auth-view");
const dashboardView = document.querySelector("#dashboard-view");
const loginForm = document.querySelector("#login-form");
const registerForm = document.querySelector("#register-form");
const feedback = document.querySelector("#auth-feedback");
const dashboardFeedback = document.querySelector("#dashboard-feedback");
const eventsList = document.querySelector("#events-list");
const emptyState = document.querySelector("#empty-state");
const participationEventsList = document.querySelector("#participation-events-list");
const participationEmpty = document.querySelector("#participation-empty");
const eventDialog = document.querySelector("#event-dialog");
const eventForm = document.querySelector("#event-form");
const eventFeedback = document.querySelector("#event-feedback");
const registrationDialog = document.querySelector("#registration-dialog");
const registrationForm = document.querySelector("#registration-form");
const registrationFeedback = document.querySelector("#registration-feedback");
let events = [];
let participationEvents = [];
let registrations = [];
let editingEventId = null;

async function api(path, options = {}) {
    const headers = new Headers(options.headers || {});
    const token = localStorage.getItem(TOKEN_KEY);
    const isPublicAuthRoute = path === "/register" || path === "/authenticate";
    if (token && !isPublicAuthRoute) headers.set("Authorization", `Bearer ${token}`);
    if (options.body) headers.set("Content-Type", "application/json");
    const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
    if (response.status === 401 && token && !isPublicAuthRoute
        && localStorage.getItem(TOKEN_KEY) === token) {
        let sessionResponse;
        try {
            sessionResponse = await fetch(`${API_BASE}/hello`, {
                headers: { Authorization: `Bearer ${token}` }
            });
        } catch {
            sessionResponse = null;
        }
        if (sessionResponse?.status === 401 && localStorage.getItem(TOKEN_KEY) === token) {
            signOut("Sua sessão expirou. Entre novamente.");
            throw new Error("Sua sessão expirou. Entre novamente.");
        }
    }
    if (response.status === 204) return null;
    const responseText = await response.text();
    let data = null;
    if (responseText) {
        try { data = JSON.parse(responseText); } catch { data = responseText; }
    }
    if (!response.ok) {
        const detail = typeof data === "string" ? data : data?.message || data?.error || data?.detail;
        throw new Error(messageForFailure(response.status, path, detail));
    }
    return data;
}

function errorForStatus(status, path) {
    if (status === 400) return "Confira os dados informados e tente novamente.";
    if (status === 401) {
        return path === "/authenticate"
            ? "E-mail ou senha inválidos."
            : "A solicitação foi recusada, mas sua sessão continua ativa.";
    }
    if (status === 403) return "Você não tem permissão para esta ação.";
    if (status === 404) return "O item solicitado não foi encontrado.";
    if (status === 409) return "Já existe uma conta com este e-mail.";
    return "Não foi possível concluir a solicitação. Tente novamente.";
}

function messageForFailure(status, path, detail) {
    const message = String(detail || "").toLocaleLowerCase("en-US");
    if (message.includes("no vacancies left for this category")) {
        return "Esta categoria está cheia. Escolha outra categoria para se inscrever.";
    }
    if (message.includes("user already registered in this event")) {
        return "Você já tem uma inscrição ativa neste evento.";
    }
    if (message.includes("cannot cancel event with active registrations")) {
        return "Não foi possível cancelar: há participantes inscritos neste evento.";
    }
    if (message.includes("only the event organizer can cancel this event")) {
        return "Este evento não pertence à sua conta e não pode ser cancelado por você.";
    }
    if (message.includes("only the event organizer can edit or cancel this event")) {
        return "A conta conectada não é a organizadora deste evento. Entre com a conta que o criou.";
    }
    if (message.includes("event has already been cancelled")) {
        return path.endsWith("/cancel")
            ? "Este evento já está cancelado."
            : "Este evento já foi cancelado e não pode ser editado.";
    }
    if (message.includes("category has registrations")) {
        return CATEGORY_REGISTRATION_ERROR;
    }
    if (message.includes("event has already started")) {
        return path.includes("/registrations")
            ? "Não foi possível se inscrever porque o evento já começou."
            : "Não foi possível alterar ou cancelar porque o evento já começou.";
    }
    if (status >= 500 && /\/events\/[^/]+\/registrations$/.test(path)) {
        return "Não foi possível concluir sua inscrição. Tente novamente.";
    }
    return detail || errorForStatus(status, path);
}

function setFeedback(element, message = "", success = false) {
    element.textContent = message;
    element.classList.toggle("success", success);
}

function showAuth(mode = "login", message = "") {
    authView.classList.remove("hidden");
    dashboardView.classList.add("hidden");
    document.querySelector("#logout-button").classList.add("hidden");
    selectAuthMode(mode);
    setFeedback(feedback, message);
}

function selectAuthMode(mode) {
    const isLogin = mode === "login";
    loginForm.classList.toggle("hidden", !isLogin);
    registerForm.classList.toggle("hidden", isLogin);
    document.querySelector("#login-tab").classList.toggle("active", isLogin);
    document.querySelector("#register-tab").classList.toggle("active", !isLogin);
    document.querySelector("#login-tab").setAttribute("aria-selected", String(isLogin));
    document.querySelector("#register-tab").setAttribute("aria-selected", String(!isLogin));
    document.querySelector("#auth-title").textContent = isLogin ? "Entre na sua conta" : "Crie seu espaço";
    document.querySelector("#auth-description").textContent = isLogin
        ? "Acesse seus eventos e continue de onde parou."
        : "Cadastre-se para começar a organizar seus eventos.";
    setFeedback(feedback);
}

function signOut(message = "") {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(NAME_KEY);
    events = [];
    showAuth("login", message);
}

async function showDashboard() {
    authView.classList.add("hidden");
    dashboardView.classList.remove("hidden");
    document.querySelector("#logout-button").classList.remove("hidden");
    const name = localStorage.getItem(NAME_KEY);
    document.querySelector("#welcome-title").innerHTML = name
        ? `Olá, ${escapeHtml(name)}<span class="brand-period">.</span>`
        : `Seus eventos<span class="brand-period">.</span>`;
    selectWorkspaceView("organizer");
    await loadEvents();
}

function selectWorkspaceView(view) {
    const panels = {
        organizer: document.querySelector("#organizer-panel"),
        participation: document.querySelector("#participation-panel"),
        registrations: document.querySelector("#registrations-panel")
    };
    document.querySelectorAll(".workspace-tab").forEach(tab => {
        const selected = tab.dataset.workspace === view;
        tab.classList.toggle("active", selected);
        tab.setAttribute("aria-selected", String(selected));
    });
    Object.entries(panels).forEach(([name, panel]) => panel.classList.toggle("hidden", name !== view));
    if (view === "participation") loadAvailableEvents();
    if (view === "registrations") loadRegistrations();
}

async function loadEvents() {
    setFeedback(dashboardFeedback);
    eventsList.replaceChildren();
    eventsList.setAttribute("aria-busy", "true");
    try {
        events = await api("/events/mine");
        renderEvents();
    } catch (error) {
        setFeedback(dashboardFeedback, error.message);
    } finally {
        eventsList.removeAttribute("aria-busy");
    }
}

function renderEvents() {
    const query = document.querySelector("#event-search").value.trim().toLocaleLowerCase("pt-BR");
    const date = document.querySelector("#event-date").value;
    const sorted = [...events].sort((a, b) => new Date(a.startDateTime) - new Date(b.startDateTime));
    const upcoming = events.filter(event => !isCancelled(event) && new Date(event.endDateTime) >= new Date()).length;
    document.querySelector("#total-count").textContent = events.length;
    document.querySelector("#upcoming-count").textContent = upcoming;
    document.querySelector("#cancelled-count").textContent = events.filter(isCancelled).length;

    const visible = sorted.filter(event => event.name.toLocaleLowerCase("pt-BR").includes(query)
        && (!date || event.startDateTime.slice(0, 10) === date));
    eventsList.replaceChildren(...visible.map(createEventCard));
    emptyState.classList.toggle("hidden", events.length > 0);
    if (events.length > 0 && visible.length === 0) {
        eventsList.innerHTML = '<p class="event-time">Nenhum evento corresponde à busca.</p>';
    }
}

function renderParticipationEvents() {
    const date = document.querySelector("#participation-date").value;
    const availableEvents = [...participationEvents]
        .sort((a, b) => new Date(a.startDateTime) - new Date(b.startDateTime))
        .filter(event => !date || event.startDateTime.slice(0, 10) === date);

    participationEventsList.replaceChildren(...availableEvents.map(createParticipationEventCard));
    participationEmpty.classList.toggle("hidden", availableEvents.length > 0);
    if (availableEvents.length === 0 && participationEvents.length > 0) {
        participationEmpty.querySelector("h2").textContent = "Nenhum evento nesta data.";
        participationEmpty.querySelector("p").textContent = "Altere a data para encontrar outros eventos disponíveis.";
    } else {
        participationEmpty.querySelector("h2").textContent = "Nenhum evento disponível.";
        participationEmpty.querySelector("p").textContent = "Os eventos disponíveis para inscrição aparecerão aqui.";
    }
}

async function loadAvailableEvents() {
    setFeedback(document.querySelector("#participation-feedback"));
    participationEventsList.setAttribute("aria-busy", "true");
    try {
        participationEvents = await api("/events/available");
        renderParticipationEvents();
    } catch (error) {
        participationEvents = [];
        renderParticipationEvents();
        setFeedback(document.querySelector("#participation-feedback"), error.message);
    } finally {
        participationEventsList.removeAttribute("aria-busy");
    }
}

function createParticipationEventCard(event, index) {
    const card = document.createElement("article");
    card.className = "event-card";
    card.style.animationDelay = `${Math.min(index, 6) * 45}ms`;
    const totalCapacity = Number(event.totalCapacity) || 0;
    const activeRegistrations = Number(event.activeRegistrations) || 0;
    const isFull = totalCapacity > 0 && activeRegistrations >= totalCapacity;
    const eventMain = document.createElement("div");
    eventMain.className = "event-main";
    const title = document.createElement("h2");
    title.className = "event-title";
    title.textContent = event.name;
    const time = document.createElement("p");
    time.className = "event-time";
    time.textContent = `${formatDate(event.startDateTime)} · ${formatTime(event.startDateTime)} às ${formatTime(event.endDateTime)}`;
    const capacity = document.createElement("p");
    capacity.className = `participation-capacity${isFull ? " full" : ""}`;
    const occupied = document.createElement("strong");
    occupied.textContent = `${activeRegistrations}/${totalCapacity}`;
    capacity.append(occupied, document.createTextNode(" vagas preenchidas"));
    if (isFull) {
        const fullLabel = document.createElement("span");
        fullLabel.className = "event-status cancelled";
        fullLabel.textContent = "LOTADO";
        capacity.append(fullLabel);
    }
    eventMain.append(title, time, capacity);

    const actions = document.createElement("div");
    actions.className = "participation-event-actions";
    const button = document.createElement("button");
    button.className = `button ${isFull ? "button-outline" : "button-primary"}`;
    button.type = "button";
    button.textContent = isFull ? "Lotado" : "Inscrever-se";
    button.disabled = isFull;
    if (isFull) {
        button.setAttribute("aria-label", `Inscrições encerradas: evento ${event.name} lotado`);
    } else {
        button.addEventListener("click", () => openRegistrationDialog(event));
    }
    actions.append(button);
    card.append(eventMain, actions);
    return card;
}

function openRegistrationDialog(event) {
    const totalCapacity = Number(event.totalCapacity) || 0;
    const activeRegistrations = Number(event.activeRegistrations) || 0;
    if (totalCapacity > 0 && activeRegistrations >= totalCapacity) return;

    const categorySelect = registrationForm.elements.categoryId;
    const categories = event.categories || [];
    document.querySelector("#registration-event-name").textContent = event.name;
    registrationForm.elements.eventId.value = event.id;
    categorySelect.replaceChildren(new Option(
        categories.length ? "Selecione uma categoria" : "Este evento não possui categorias disponíveis",
        ""
    ));
    for (const category of categories) {
        categorySelect.add(new Option(
            `${category.name} · ${formatCurrency(category.price)} · ${category.capacity} vagas`,
            category.id
        ));
    }
    categorySelect.disabled = categories.length === 0;
    registrationForm.querySelector("button[type=submit]").disabled = categories.length === 0;
    setFeedback(registrationFeedback);
    registrationDialog.showModal();
}

function localDateTimeAfter(hoursFromNow) {
    const date = new Date(Date.now() + hoursFromNow * 60 * 60 * 1000);
    const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return `${localDate.toISOString().slice(0, 16)}:00`;
}

async function createSampleData() {
    if (!window.confirm("Criar três eventos de demonstração com categorias na sua conta?")) return;
    const button = document.querySelector("#sample-data-button");
    button.disabled = true;
    setFeedback(dashboardFeedback);
    const samples = [
        {
            name: "Demo | Encontro de ideias",
            startsIn: 24,
            categories: [
                { name: "Acesso geral", capacity: 80, price: 45 },
                { name: "Estudante", capacity: 30, price: 22.5 }
            ]
        },
        {
            name: "Demo | Oficina criativa",
            startsIn: 48,
            categories: [
                { name: "Participante", capacity: 40, price: 35 },
                { name: "Meia-entrada", capacity: 20, price: 17.5 }
            ]
        },
        {
            name: "Demo | Festival de comunidades",
            startsIn: 72,
            categories: [
                { name: "Passaporte", capacity: 120, price: 60 },
                { name: "Acesso diário", capacity: 60, price: 25 }
            ]
        }
    ];

    try {
        events = await api("/events/mine");
        for (const sample of samples) {
            let event = events.find(item => item.name === sample.name);
            if (!event) {
                event = await api("/events", {
                    method: "POST",
                    body: JSON.stringify({
                        name: sample.name,
                        startDateTime: localDateTimeAfter(sample.startsIn),
                        endDateTime: localDateTimeAfter(sample.startsIn + 2)
                    })
                });
                event.categories = [];
            }
            for (const category of sample.categories) {
                if (event.categories?.some(item => item.name === category.name)) continue;
                await api(`/events/${event.id}/categories`, {
                    method: "POST",
                    body: JSON.stringify(category)
                });
            }
            events = await api("/events/mine");
        }
        renderEvents();
        setFeedback(dashboardFeedback, "Dados de demonstração carregados.", true);
    } catch (error) {
        setFeedback(dashboardFeedback, error.message);
    } finally {
        button.disabled = false;
    }
}

async function submitRegistration(event) {
    event.preventDefault();
    const eventId = registrationForm.elements.eventId.value.trim();
    const categoryId = registrationForm.elements.categoryId.value.trim();
    const submit = registrationForm.querySelector("button[type=submit]");
    submit.disabled = true;
    setFeedback(registrationFeedback);
    try {
        await api(`/events/${eventId}/registrations`, {
            method: "POST",
            body: JSON.stringify({ categoryId })
        });
        registrationDialog.close();
        registrationForm.reset();
        await loadAvailableEvents();
        selectWorkspaceView("registrations");
        setFeedback(document.querySelector("#registrations-feedback"), "Inscrição confirmada.", true);
    } catch (error) {
        setFeedback(registrationFeedback, error.message);
    } finally {
        if (registrationDialog.open) submit.disabled = false;
    }
}

async function loadRegistrations() {
    setFeedback(document.querySelector("#registrations-feedback"));
    const filter = document.querySelector("#registration-filter").value;
    try {
        registrations = await api(`/registrations/details?filter=${encodeURIComponent(filter)}`);
        renderRegistrations();
    } catch (error) {
        setFeedback(document.querySelector("#registrations-feedback"), error.message);
    }
}

function renderRegistrations() {
    const visibleRegistrations = [...registrations]
        .sort((a, b) => new Date(a.startDateTime) - new Date(b.startDateTime));
    const list = document.querySelector("#registrations-list");
    const totalCounts = visibleRegistrations.reduce((counts, registration) => {
        counts.set(registration.eventId, (counts.get(registration.eventId) || 0) + 1);
        return counts;
    }, new Map());
    const occurrenceCounts = new Map();
    list.replaceChildren(...visibleRegistrations.map(registration => {
        const occurrence = (occurrenceCounts.get(registration.eventId) || 0) + 1;
        occurrenceCounts.set(registration.eventId, occurrence);
        const card = document.createElement("article");
        card.className = "registration-card";
        const details = document.createElement("div");
        const title = document.createElement("h2");
        title.textContent = registration.eventName;
        const time = document.createElement("p");
        time.textContent = `${formatDate(registration.startDateTime)} · ${formatTime(registration.startDateTime)} às ${formatTime(registration.endDateTime)} · Inscrição ${occurrence} de ${totalCounts.get(registration.eventId)}`;
        details.append(title, time);
        const content = document.createElement("div");
        content.className = "registration-main";
        content.append(details);
        card.append(content);
        const eventHasNotStarted = new Date(registration.startDateTime) > new Date();
        const statusText = registration.status === "CANCELADA"
            ? "CANCELADA"
            : registration.eventStatus === "CANCELLED"
                ? "EVENTO CANCELADO"
                : eventHasNotStarted ? "ATIVA" : "ENCERRADA";
        const status = document.createElement("span");
        status.className = `event-status${statusText === "CANCELADA" ? " cancelled" : ""}`;
        status.textContent = statusText;
        content.append(status);
        const categoryLabel = document.createElement("span");
        categoryLabel.className = "registration-category";
        categoryLabel.textContent = `${registration.categoryName} · ${formatCurrency(registration.price)}`;
        content.append(categoryLabel);
        if (registration.status === "ATIVA" && statusText === "ATIVA" && eventHasNotStarted) {
            const cancelButton = document.createElement("button");
            cancelButton.className = "text-button";
            cancelButton.type = "button";
            cancelButton.textContent = "Cancelar inscrição";
            cancelButton.addEventListener("click", () => cancelRegistration(registration, cancelButton));
            card.append(cancelButton);
        }
        return card;
    }));
    document.querySelector("#registrations-empty").classList.toggle("hidden", registrations.length > 0);
}

async function cancelRegistration(registration, button) {
    if (!window.confirm(`Cancelar sua inscrição em "${registration.eventName}"?`)) return;
    button.disabled = true;
    try {
        await api(`/events/registrations/${registration.registrationId}/cancel`, { method: "POST" });
        await loadRegistrations();
        setFeedback(document.querySelector("#registrations-feedback"), "Inscrição cancelada.", true);
    } catch (error) {
        setFeedback(document.querySelector("#registrations-feedback"), error.message);
        button.disabled = false;
    }
}

function createEventCard(event, index) {
    const card = document.createElement("article");
    card.className = "event-card";
    card.style.animationDelay = `${Math.min(index, 6) * 45}ms`;
    const cancelled = isCancelled(event);
    const eventMain = document.createElement("div");
    eventMain.className = "event-main";
    const titleLine = document.createElement("div");
    titleLine.className = "event-title-line";
    const title = document.createElement("h3");
    title.className = "event-title";
    title.textContent = event.name;
    const status = document.createElement("span");
    status.className = `event-status${cancelled ? " cancelled" : ""}`;
    status.textContent = cancelled ? "CANCELADO" : new Date(event.endDateTime) < new Date() ? "CONCLUÍDO" : "ATIVO";
    titleLine.append(title, status);
    const time = document.createElement("p");
    time.className = "event-time";
    time.textContent = `${formatDate(event.startDateTime)} · ${formatTime(event.startDateTime)} às ${formatTime(event.endDateTime)}`;
    eventMain.append(titleLine, time);

    const actions = document.createElement("div");
    actions.className = "event-actions";
    if (!cancelled) {
        const editButton = document.createElement("button");
        editButton.className = "button button-outline";
        editButton.type = "button";
        editButton.textContent = "Editar";
        editButton.addEventListener("click", () => openEventDialog(event));
        const cancelButton = document.createElement("button");
        cancelButton.className = "text-button";
        cancelButton.type = "button";
        cancelButton.textContent = "Cancelar evento";
        cancelButton.addEventListener("click", clickEvent => {
            clickEvent.preventDefault();
            clickEvent.stopPropagation();
            cancelEvent(event, cancelButton);
        });
        actions.append(editButton, cancelButton);
    }
    card.append(eventMain, actions);
    card.append(createCategoryManager(event));
    return card;
}

function createCategoryManager(event) {
    const fragment = document.querySelector("#category-template").content.cloneNode(true);
    const manager = fragment.querySelector(".category-manager");
    manager.querySelector(".category-count").textContent = `(${event.categories?.length || 0})`;
    const form = manager.querySelector(".category-form");
    const nameInput = manager.querySelector("[data-category-name]");
    const nameLabel = manager.querySelector("[data-category-label]");
    const inputId = `category-name-${event.id}`;
    nameInput.id = inputId;
    nameLabel.htmlFor = inputId;
    form.addEventListener("submit", async e => {
        e.preventDefault();
        const submit = form.querySelector("button[type=submit]");
        submit.disabled = true;
        try {
            await api(`/events/${event.id}/categories`, {
                method: "POST",
                body: JSON.stringify({
                    name: nameInput.value.trim(),
                    capacity: Number(form.querySelector("[data-category-capacity]").value),
                    price: Number(form.querySelector("[data-category-price]").value)
                })
            });
            await loadEvents();
            setFeedback(dashboardFeedback, "Categoria adicionada.", true);
        } catch (error) {
            setFeedback(dashboardFeedback, error.message);
            submit.disabled = false;
        }
    });
    const list = manager.querySelector(".category-list");
    for (const category of event.categories || []) {
        const item = document.createElement("li");
        item.className = "category-item";
        const description = document.createElement("span");
        description.textContent = `${category.name} · ${category.capacity} vagas · ${formatCurrency(category.price)}`;
        const actions = document.createElement("div");
        actions.className = "category-actions";
        const remove = document.createElement("button");
        remove.className = "text-button";
        remove.type = "button";
        remove.textContent = "Remover";
        remove.setAttribute("aria-label", `Remover categoria ${category.name}`);
        remove.addEventListener("click", async () => {
            if (!window.confirm(`Remover a categoria "${category.name}"?`)) return;
            try {
                await api(`/events/${event.id}/categories/${category.id}`, { method: "DELETE" });
                await loadEvents();
                setFeedback(dashboardFeedback, "Categoria removida.", true);
            } catch (error) {
                if (error.message === CATEGORY_REGISTRATION_ERROR) {
                    window.alert(error.message);
                } else {
                    setFeedback(dashboardFeedback, error.message);
                }
            }
        });
        actions.append(remove);
        item.append(description, actions);
        list.append(item);
    }
    return fragment;
}

function openEventDialog(event = null) {
    editingEventId = event?.id || null;
    document.querySelector("#event-dialog-title").textContent = event ? "Editar evento" : "Novo evento";
    document.querySelector("#event-name").value = event?.name || "";
    document.querySelector("#event-start").value = event ? toDateTimeInput(event.startDateTime) : "";
    document.querySelector("#event-end").value = event ? toDateTimeInput(event.endDateTime) : "";
    setFeedback(eventFeedback);
    eventDialog.showModal();
}

async function saveEvent(event) {
    event.preventDefault();
    const submit = eventForm.querySelector("button[type=submit]");
    submit.disabled = true;
    const payload = {
        name: document.querySelector("#event-name").value.trim(),
        startDateTime: toLocalDateTime(document.querySelector("#event-start").value),
        endDateTime: toLocalDateTime(document.querySelector("#event-end").value)
    };
    try {
        await api(editingEventId ? `/events/${editingEventId}` : "/events", {
            method: editingEventId ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });
        eventDialog.close();
        await loadEvents();
        setFeedback(dashboardFeedback, editingEventId ? "Evento atualizado." : "Evento criado.", true);
    } catch (error) {
        setFeedback(eventFeedback, error.message);
    } finally {
        submit.disabled = false;
    }
}

async function cancelEvent(event, button) {
    if (!window.confirm(`Cancelar o evento "${event.name}"?`)) return;
    button.disabled = true;
    setFeedback(dashboardFeedback, "Cancelando evento...");
    try {
        const cancelledEvent = await api(`/events/${event.id}/cancel`, { method: "POST" });
        events = events.map(current => current.id === event.id
            ? { ...current, ...cancelledEvent, status: cancelledEvent?.status || "CANCELLED" }
            : current);
        renderEvents();
        setFeedback(dashboardFeedback, "Evento cancelado.", true);
    } catch (error) {
        setFeedback(dashboardFeedback, error.message);
        window.alert(error.message);
        button.disabled = false;
    }
}

function isCancelled(event) {
    return event.status === "CANCELLED" || event.cancelled === true;
}

function formatDate(value) {
    return new Intl.DateTimeFormat("pt-BR", { dateStyle: "medium" }).format(new Date(value));
}

function formatTime(value) {
    return new Intl.DateTimeFormat("pt-BR", { hour: "2-digit", minute: "2-digit" }).format(new Date(value));
}

function formatCurrency(value) {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value);
}

function toDateTimeInput(value) {
    return String(value).slice(0, 16);
}

function toLocalDateTime(value) {
    return value.length === 16 ? `${value}:00` : value;
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
    })[character]);
}

document.querySelector("#login-tab").addEventListener("click", () => selectAuthMode("login"));
document.querySelector("#register-tab").addEventListener("click", () => selectAuthMode("register"));
document.querySelector("#logout-button").addEventListener("click", () => signOut());
document.querySelector("#sample-data-button").addEventListener("click", createSampleData);
document.querySelector("#new-event-button").addEventListener("click", () => openEventDialog());
document.querySelector("#empty-create-button").addEventListener("click", () => openEventDialog());
document.querySelector("#event-search").addEventListener("input", renderEvents);
document.querySelector("#event-date").addEventListener("input", renderEvents);
document.querySelector("#participation-date").addEventListener("input", renderParticipationEvents);
document.querySelectorAll(".workspace-tab").forEach(tab => tab.addEventListener("click", () => selectWorkspaceView(tab.dataset.workspace)));
document.querySelectorAll("[data-workspace-link]").forEach(button => button.addEventListener("click", () => selectWorkspaceView(button.dataset.workspaceLink)));
registrationForm.addEventListener("submit", submitRegistration);
document.querySelector("#registration-filter").addEventListener("change", loadRegistrations);
eventForm.addEventListener("submit", saveEvent);
document.querySelectorAll("[data-close-dialog]").forEach(button => button.addEventListener("click", () => eventDialog.close()));
document.querySelectorAll("[data-close-registration-dialog]").forEach(button => button.addEventListener("click", () => registrationDialog.close()));

loginForm.addEventListener("submit", async event => {
    event.preventDefault();
    const submit = loginForm.querySelector("button[type=submit]");
    submit.disabled = true;
    setFeedback(feedback);
    const formData = new FormData(loginForm);
    try {
        const result = await api("/authenticate", {
            method: "POST",
            body: JSON.stringify({ username: formData.get("username"), password: formData.get("password") })
        });
        localStorage.setItem(TOKEN_KEY, result.token);
        await showDashboard();
    } catch (error) {
        setFeedback(feedback, error.message);
    } finally {
        submit.disabled = false;
    }
});

registerForm.addEventListener("submit", async event => {
    event.preventDefault();
    const submit = registerForm.querySelector("button[type=submit]");
    submit.disabled = true;
    setFeedback(feedback);
    const formData = new FormData(registerForm);
    const name = `${formData.get("name").trim()} ${formData.get("lastname").trim()}`.trim();
    const email = formData.get("email");
    const password = formData.get("password");
    try {
        await api("/register", {
            method: "POST",
            body: JSON.stringify({ name: formData.get("name").trim(), lastname: formData.get("lastname").trim(), email, password })
        });
        const result = await api("/authenticate", {
            method: "POST",
            body: JSON.stringify({ username: email, password })
        });
        localStorage.setItem(TOKEN_KEY, result.token);
        localStorage.setItem(NAME_KEY, name.split(" ")[0]);
        await showDashboard();
    } catch (error) {
        setFeedback(feedback, error.message);
    } finally {
        submit.disabled = false;
    }
});

if (localStorage.getItem(TOKEN_KEY)) {
    showDashboard().catch(() => signOut("Sua sessão expirou. Entre novamente."));
}