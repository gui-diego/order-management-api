# Order Management API

[![CI](https://github.com/gui-diego/order-management-api/actions/workflows/ci.yaml/badge.svg)](https://github.com/gui-diego/order-management-api/actions/workflows/ci.yaml)

API REST de categorias, produtos e pedidos. Ao criar o pedido, o estoque é reservado. Um job simula o pagamento: até R$ 10.000,00 aprova; acima disso recusa e devolve o estoque. O acesso é por JWT, com os papéis `ADMIN` e `CUSTOMER`.

## Como baixar

```bash
git clone https://github.com/gui-diego/order-management-api.git
cd order-management-api
```

## Como subir

Com o Docker instalado e ligado, na pasta do projeto:

```bash
docker compose up --build
```

A primeira vez demora mais, porque a imagem compila o projeto. A API sobe em `http://localhost:8080`. O MySQL do compose fica na porta 3307.

- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Health: `http://localhost:8080/actuator/health`

Usuário administrador criado pela migration:

- e-mail: `admin@order.local`
- senha: `admin123`

O cliente é criado em `POST /auth/register`. No Swagger, use **Authorize** com o token retornado no login.

Para parar:

```bash
docker compose down
```

`docker compose down -v` também apaga o volume do banco.

## Testes

```bash
./mvnw test
```

No Windows: `.\mvnw.cmd test`.

Os testes de controller usam o MySQL de `DB_URL`. No GitHub Actions, o workflow sobe um MySQL próprio para a suíte.

## Tecnologias

* Java 21
* Spring Boot
* Spring Web
* Spring Security e JWT
* Spring Data JPA
* Bean Validation
* Maven
* MySQL 8
* Flyway
* JUnit e Mockito
* GitHub Actions
* Swagger / OpenAPI
* Docker Compose
