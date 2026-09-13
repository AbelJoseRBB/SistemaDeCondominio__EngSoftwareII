# 📁 Estrutura do Projeto – Sistema de Gestão de Condomínio
---

## 🗂️ Visão Geral da Estrutura

```
CondoManager/
├── pom.xml                          ← Configuração do Maven (dependências, plugins)
├── .gitignore                       ← Arquivos ignorados pelo Git
├── README.md                        ← Apresentação do projeto no GitHub
│
├── docs/
│   ├── documentacao-tecnica.md      ← Como instalar e configurar o sistema
│   └── diagramas/
│       ├── atividades/              ← Diagramas de atividades (3 requisitos marcados com *)
│       └── sequencia/               ← Diagramas de sequência (3 requisitos marcados com *)
│
└── src/
    ├── main/
    │   ├── java/com/condominio/
    │   │   ├── Main.java            ← Ponto de entrada da aplicação
    │   │   ├── model/               ← Entidades do sistema
    │   │   ├── dao/                 ← Acesso ao banco de dados (SQL/JDBC)
    │   │   ├── service/             ← Regras de negócio
    │   │   ├── controller/          ← Controllers das telas FXML
    │   │   └── util/                ← Utilitários (conexão, sessão)
    │   └── resources/
    │       ├── fxml/                ← Telas da interface (FXML)
    │       ├── css/                 ← Estilos visuais
    │       ├── images/              ← Ícones e imagens
    │       └── sql/                 ← Scripts do banco de dados
    └── test/
        └── java/com/condominio/     ← Testes automatizados (JUnit 5)
```

---

## 🏗️ Arquitetura MVC – Como as Camadas se Comunicam

```
[FXML / View] ←→ [Controller] → [Service] → [DAO] → [MySQL]
```

| Camada | Pasta | O que faz |
|--------|-------|-----------|
| **View** | `resources/fxml/` | Telas visuais em FXML (editadas manualmente ou no SceneBuilder) |
| **Controller** | `controller/` | Liga a tela ao sistema; trata cliques e eventos do usuário |
| **Service** | `service/` | Contém as **regras de negócio** (validações, conflitos, etc.) |
| **DAO** | `dao/` | Executa as **consultas SQL** e retorna objetos Java |
| **Model** | `model/` | Representa as entidades (Unidade, Morador, Reserva…) |
| **Util** | `util/` | Ferramentas compartilhadas (conexão BD, sessão do usuário) |

---

## 📦 Arquivos Importantes

### `pom.xml`
Arquivo de configuração do Maven. Define:
- **Java 17** como versão de compilação
- **JavaFX 21** para a interface gráfica
- **MySQL Connector** para o banco de dados
- **BCrypt** para hash seguro de senhas
- **JUnit 5** para testes

### `src/main/java/com/condominio/Main.java`
Ponto de entrada da aplicação. Inicializa o JavaFX e carrega a tela de Login.

### `src/main/resources/sql/schema.sql`
Script SQL completo com:
- Criação do banco `condominio_db`
- Todas as tabelas (usuario, unidade, morador, taxa, reserva, ocorrencia, manutencao, veiculo)
- Usuário admin inicial (login: `admin`, senha: `admin123`)

### `src/main/java/com/condominio/util/DBConnection.java`
Gerencia a conexão com o MySQL. **Configure aqui sua senha do banco antes de rodar.**

### `src/main/java/com/condominio/util/SessionManager.java`
Armazena o usuário logado durante a execução. Usado para controle de acesso por perfil.

### `src/main/java/com/condominio/service/AuthService.java`
Login com verificação de senha usando **BCrypt** (hash seguro).

### `src/main/java/com/condominio/service/ReservaService.java`
Já implementa a lógica de **conflito de horário** nas reservas de áreas comuns.

---

## 📋 Módulos do Sistema

| Módulo | Controller | FXML | Service | DAO | Model |
|--------|-----------|------|---------|-----|-------|
| Login | `LoginController` | `Login.fxml` | `AuthService` | `UsuarioDAO` | `Usuario` |
| Unidades | `UnidadeListController` / `UnidadeFormController` | `UnidadeList.fxml` / `UnidadeForm.fxml` | `UnidadeService` | `UnidadeDAO` | `Unidade` |
| Moradores | `MoradorListController` / `MoradorFormController` | `MoradorList.fxml` / `MoradorForm.fxml` | `MoradorService` | `MoradorDAO` | `Morador` |
| Taxas | `TaxaListController` / `TaxaFormController` | ... | `TaxaService` | `TaxaDAO` | `Taxa` |
| Reservas ⭐ | `ReservaListController` / `ReservaFormController` | ... | `ReservaService` | `ReservaDAO` | `Reserva` |
| Ocorrências ⭐ | `OcorrenciaListController` / `OcorrenciaFormController` | ... | `OcorrenciaService` | `OcorrenciaDAO` | `Ocorrencia` |
| Manutenções | ... | ... | `ManutencaoService` | `ManutencaoDAO` | `Manutencao` |
| Veículos | ... | ... | `VeiculoService` | `VeiculoDAO` | `Veiculo` |
| Relatórios | ... | ... | `RelatorioService` | — | — |

> ⭐ = Módulos que precisam de diagrama de atividades + sequência (junto com Unidades)

---

## 🌿 Git – Trabalhando com Branches e Pull Requests

### 1. Configuração Inicial

```bash
# Clone o repositório (feito uma vez por cada desenvolvedor)
git clone https://github.com/AbelJoseRBB/SistemaDeCondominio__EngSoftwareII.git
cd CondoManager
```

### 2. Estratégia de Branches Recomendada

```
main          ← Código estável/entregável. NUNCA commitar direto aqui.
  └── develop ← Branch de integração. Todas as features vão aqui primeiro.
        ├── feature/cadastro-unidade    ← Dev A trabalhando em Unidades
        ├── feature/reservas-areas      ← Dev B trabalhando em Reservas
        └── feature/login-autenticacao  ← Dev C trabalhando em Login
```

### 3. Fluxo de Trabalho Diário

```bash
# 1. Antes de começar, atualize o develop local
git checkout develop
git pull origin develop

# 2. Crie sua branch para a funcionalidade
git checkout -b feature/nome-da-funcionalidade

# 3. Faça suas alterações...

# 4. Adicione os arquivos modificados
git add .

# 5. Faça o commit com uma mensagem clara
git commit -m "feat: adiciona formulário de cadastro de unidade"

# 6. Envie sua branch para o GitHub
git push origin feature/nome-da-funcionalidade
```

### 4. Criando um Pull Request (PR) no GitHub

1. Acesse o repositório no GitHub
2. Clique em **"Compare & pull request"** (aparece automaticamente após o push)
3. Defina:
   - **Base:** `develop` ← seu PR vai para o develop, não para o main!
   - **Compare:** `feature/sua-branch`
4. Escreva uma descrição do que foi feito
5. Solicite revisão dos colegas (Reviewers)
6. Após aprovação, clique em **"Merge pull request"**

### 5. Convenção de Nomes para Branches

| Prefixo | Uso | Exemplo |
|---------|-----|---------|
| `feature/` | Nova funcionalidade | `feature/cadastro-morador` |
| `fix/` | Correção de bug | `fix/conflito-reserva` |
| `docs/` | Documentação | `docs/diagrama-sequencia` |
| `refactor/` | Refatoração de código | `refactor/dao-unidade` |

### 6. Convenção de Commits (Padrão)

```
feat: nova funcionalidade
fix: correção de bug
docs: documentação
style: formatação (sem mudança de lógica)
refactor: refatoração
test: testes
```

### 7. Resolvendo Conflitos

```bash
# Se houver conflito ao dar merge, primeiro atualize sua branch
git checkout feature/sua-branch
git merge develop   # traz o que veio do develop para sua branch

# O Git vai marcar os arquivos em conflito.
# Edite-os manualmente, depois:
git add .
git commit -m "fix: resolve conflito de merge"
git push origin feature/sua-branch
```

---

## ⚙️ Como Abrir no IntelliJ IDEA

1. **File → Open** → selecione a pasta `CondoManager`
2. O IntelliJ detecta o `pom.xml` automaticamente
3. Aguarde o Maven baixar as dependências
4. Configure o **Project SDK** para Java 17 (File → Project Structure → SDK)
5. Execute: `mvn javafx:run` no terminal ou configure um Run Configuration

