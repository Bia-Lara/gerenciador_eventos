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
3. Use **Gerar dados de exemplo** para criar três eventos e categorias na sua conta, usando os endpoints existentes.
4. Na aba **Organizar**, filtre os eventos da conta por data. Para inscrição, selecione um evento futuro e uma categoria na aba **Inscrever-se**; o formulário também pode ser preenchido pelo botão de inscrição em uma categoria do evento.
5. Consulte seus eventos inscritos em **Minhas inscrições** ou organize eventos na aba **Organizar**. Em cada evento organizado, expanda **Categorias** para gerenciar capacidade e preço.
6. Use **Sair** para encerrar a sessão neste navegador.

O JWT é armazenado no `localStorage` do navegador e enviado no cabeçalho `Authorization: Bearer ...` das chamadas protegidas. O token é apagado ao sair ou quando a API responde com HTTP 401.

## API utilizada

A interface usa a API na mesma origem (`/api/v1`):

- `POST /register` e `POST /authenticate` para cadastro e autenticação.
- `GET /events/mine`, `POST /events`, `PUT /events/{eventId}` e `POST /events/{eventId}/cancel` para organização.
- `POST /events/{eventId}/categories` e `DELETE /events/{eventId}/categories/{categoryId}` para categorias.
- `POST /events/{eventId}/registrations` para se inscrever e `GET /registrations?filter=TODOS|ATIVOS|INATIVOS` para consultar eventos inscritos.
- `POST /events/registrations/{registrationId}/cancel` para cancelar uma inscrição ainda não iniciada.

O back-end atual não fornece um catálogo global de eventos nem retorna o ID da inscrição na consulta de inscrições. O front filtra por data os eventos que a conta consegue consultar e mantém localmente os IDs das inscrições feitas nesta sessão/navegador para oferecer cancelamento.

A documentação OpenAPI fica disponível em [http://localhost:8091/api/v1/api-docs](http://localhost:8091/api/v1/api-docs). As rotas de API protegidas continuam exigindo autenticação; somente cadastro, login, documentação e arquivos estáticos da interface são públicos.

## Executar os testes

```bash
./mvnw test
```
