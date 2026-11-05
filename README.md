# Gerenciador de eventos

Aplicação Spring Boot para gerenciamento de eventos, categorias e inscrições, com autenticação JWT e interface Web integrada.

## Requisitos

- Java 21
- macOS/Linux: terminal com o wrapper Maven (`./mvnw`); no Windows, use `mvnw.cmd`

## Configuração e execução

Na raiz do projeto, inicie a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows:

```bat
mvnw.cmd spring-boot:run
```

Abra [http://localhost:8091](http://localhost:8091). O front-end está em `src/main/resources/static` e é servido pelo próprio Spring Boot; não é necessário instalar Node.js, configurar CORS ou iniciar outro servidor.

## Usar a interface

1. Selecione **Criar conta** e informe nome, sobrenome, e-mail e senha. Após o cadastro, a interface autentica a conta automaticamente.
2. Entre com e-mail e senha para abrir o painel.
3. Use **Gerar dados de exemplo** para criar três eventos e categorias na sua conta.
4. Na aba **Organizar**, filtre os eventos da conta por data. Para se inscrever, selecione um evento futuro de outro organizador e uma categoria na aba **Inscrever-se**. O organizador não pode se inscrever no próprio evento.
5. Consulte seus eventos inscritos em **Minhas inscrições** ou organize eventos na aba **Organizar**. Em cada evento organizado, expanda **Categorias** para gerenciar capacidade e preço.
6. Use **Sair** para encerrar a sessão neste navegador.

O JWT é armazenado no `localStorage` do navegador e enviado no cabeçalho `Authorization: Bearer ...` das chamadas protegidas. O token é apagado ao sair ou quando a API responde com HTTP 401.

## API utilizada

A interface consome a API na mesma origem (`/api/v1`). Todos os endpoints, exceto os de autenticação, exigem usuário autenticado.

**Autenticação**
- `POST /register`: cadastra um novo usuário.
- `POST /authenticate`: autentica o usuário.

**Eventos**
- `GET /events/mine`: lista os eventos organizados pelo usuário.
- `GET /events/available`: lista eventos futuros de outros organizadores disponíveis para inscrição, com o total de inscrições ativas.
- `POST /events`: cria um evento.
- `PUT /events/{eventId}`: edita nome e datas de início e fim.
- `POST /events/{eventId}/cancel`: cancela um evento.

**Categorias**
- `POST /events/{eventId}/categories`: cria uma categoria no evento.
- `DELETE /events/{eventId}/categories/{categoryId}`: remove uma categoria do evento.

**Inscrições**
- `POST /events/{eventId}/registrations`: inscreve o usuário em uma categoria do evento (`categoryId` no corpo).
- `POST /events/registrations/{registrationId}/cancel`: cancela uma inscrição de evento ainda não iniciado.
- `GET /registrations?filter=`: lista os eventos em que o usuário está inscrito.
- `GET /registrations/details?filter=`: lista as inscrições do usuário com ID, evento, categoria e status.

O parâmetro `filter` aceita `TODOS` (padrão), `ATIVOS` ou `INATIVOS`.

O banco inclui um evento futuro de demonstração pertencente a outro usuário simulado, disponível para inscrições. O back-end valida a regra de não permitir autoinscrição mesmo que a interface seja contornada.

A documentação OpenAPI fica disponível em [http://localhost:8091/api/v1/api-docs](http://localhost:8091/api/v1/api-docs). As rotas de API protegidas continuam exigindo autenticação; somente cadastro, login, documentação e arquivos estáticos da interface são públicos.

## Executar os testes

```bash
./mvnw test
```
