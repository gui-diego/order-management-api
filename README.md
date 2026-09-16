# Order Management API

[![CI](https://github.com/gui-diego/order-management-api/actions/workflows/ci.yaml/badge.svg)](https://github.com/gui-diego/order-management-api/actions/workflows/ci.yaml)

API REST para gerenciamento de pedidos e produtos, desenvolvida com Java e Spring Boot.

O projeto está sendo desenvolvido de forma incremental, com foco na implementação de um fluxo de gerenciamento de produtos e pedidos, abrangendo categorias, produtos, pedidos e itens do pedido.

## 🚧 Status

Em desenvolvimento

### Atualmente implementado

- CRUD de produtos
- CRUD de categorias
- Criação de pedidos
- Validação dos dados de entrada
- Tratamento global de exceções
- Testes unitários e de integração
- CI com GitHub Actions para execução automática dos testes
- Documentação da API com Swagger

### Pré-requisitos

- Java 21
- MySQL 8 ou superior

### Configuração do banco de dados

Crie um banco de dados MySQL chamado `order_management`:

```sql
CREATE DATABASE order_management;
```

Caso necessário, ajuste a URL de conexão, o usuário e a senha em `src/main/resources/application.yaml`.

O schema é versionado pelo Flyway. Na primeira inicialização com o banco vazio, a migration `V1__create_order_management_schema.sql` cria todas as tabelas automaticamente. O Hibernate está configurado apenas para validar o schema;

### Inicialização

No diretório raiz do projeto, execute:

```bash
./mvnw spring-boot:run
```

No Windows, use:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação será iniciada em `http://localhost:8080`.

## 🧪 Testes

Para executar os testes unitários e de integração:

```bash
./mvnw test
```

No Windows, use:

```powershell
.\mvnw.cmd test
```

## 📚 Documentação

A documentação interativa da API está disponível no Swagger UI após iniciar a aplicação:

`http://localhost:8080/swagger-ui/index.html`

## 🛠️ Tecnologias

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Bean Validation
* Maven
* MySQL
* JUnit
* Mockito
* GitHub Actions
* Swagger / OpenAPI
* Flyway