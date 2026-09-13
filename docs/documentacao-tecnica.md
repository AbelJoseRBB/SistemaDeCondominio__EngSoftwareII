# Documentacao Tecnica - CondoManager - Sistema de Gestao de Condominio

## Arquitetura

O projeto segue o padrao arquitetural **MVC (Model-View-Controller)** adaptado para JavaFX:

```
View (FXML) --> Controller (Java) --> Service (Java) --> DAO (Java) --> Banco de Dados (MySQL)
```

## Configuracao do Ambiente

### Pre-requisitos
- JDK 17+: https://adoptium.net/
- Maven 3.8+: https://maven.apache.org/
- MySQL 8.0+: https://dev.mysql.com/downloads/

### Banco de Dados
1. Instale e inicie o MySQL
2. Execute o script: `src/main/resources/sql/schema.sql`
3. Configure a conexao em: `src/main/java/com/condominio/util/DBConnection.java`

### Executar o Projeto
```bash
mvn javafx:run
```

## Estrutura de Pacotes

| Pacote | Responsabilidade |
|--------|-----------------|
| `com.condominio.model` | Classes de entidade (POJO) |
| `com.condominio.dao` | Acesso ao banco de dados (JDBC) |
| `com.condominio.service` | Regras de negocio |
| `com.condominio.controller` | Controllers das telas FXML |
| `com.condominio.util` | Utilitarios (conexao, sessao) |

## Login Padrao
- **Login:** admin
- **Senha:** admin123
- **IMPORTANTE:** Altere a senha em producao!
