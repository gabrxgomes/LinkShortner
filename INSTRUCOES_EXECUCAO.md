# 📖 Instruções de Execução - Link Shortener

## 🚀 Como Executar o Projeto no VSCode

---

## 📋 PRÉ-REQUISITOS

Antes de começar, certifique-se de ter instalado:

1. **Java 17 ou superior**
   - Verifique: `java -version`
   - Download: https://www.oracle.com/java/technologies/downloads/

2. **Maven 3.6 ou superior**
   - Verifique: `mvn -version`
   - Download: https://maven.apache.org/download.cgi
   - **IMPORTANTE:** Adicione o Maven ao PATH do Windows

3. **Visual Studio Code**
   - Download: https://code.visualstudio.com/

4. **Extensões do VSCode (Recomendadas):**
   - Extension Pack for Java (Microsoft)
   - Spring Boot Extension Pack (VMware)
   - Maven for Java

---

## 🛠️ INSTALAÇÃO DO MAVEN NO WINDOWS

Se você não tem o Maven instalado:

1. Baixe o Maven: https://maven.apache.org/download.cgi
   - Escolha: `apache-maven-3.x.x-bin.zip`

2. Extraia para: `C:\Program Files\Apache\maven`

3. Adicione ao PATH:
   - Pesquise "Variáveis de Ambiente" no Windows
   - Edite a variável `Path` do sistema
   - Adicione: `C:\Program Files\Apache\maven\bin`
   - Clique em OK

4. Crie a variável JAVA_HOME:
   - Crie nova variável de sistema: `JAVA_HOME`
   - Valor: caminho da instalação do Java (ex: `C:\Program Files\Java\jdk-17`)

5. Reinicie o terminal e teste: `mvn -version`

---

## 📂 ESTRUTURA DO PROJETO

```
LinkEncurterProd/
├── src/
│   └── main/
│       ├── java/com/linkshortener/
│       │   ├── LinkShortenerApplication.java  (Main)
│       │   ├── config/
│       │   │   └── SecurityConfig.java
│       │   ├── controller/
│       │   │   └── LinkController.java
│       │   ├── dto/
│       │   │   ├── CreateLinkRequest.java
│       │   │   └── LinkResponse.java
│       │   ├── model/
│       │   │   └── Link.java
│       │   ├── repository/
│       │   │   └── LinkRepository.java
│       │   ├── scheduler/
│       │   │   └── LinkCleanupScheduler.java
│       │   └── service/
│       │       ├── LinkService.java
│       │       └── UrlValidator.java
│       └── resources/
│           ├── application.properties
│           ├── application-production.properties
│           └── static/
│               ├── index.html
│               ├── styles.css
│               ├── script.js
│               └── error.html
├── pom.xml
├── vercel.json
├── .gitignore
├── readme.md
├── RELATORIO_SEGURANCA.md
└── INSTRUCOES_EXECUCAO.md (este arquivo)
```

---

## ▶️ EXECUTANDO NO LOCALHOST (Opção 1 - Maven via Terminal)

### Passo 1: Abrir o projeto no VSCode

```bash
cd D:\Estudo\Java\LinkEncurterProd
code .
```

### Passo 2: Compilar o projeto

Abra o terminal integrado no VSCode (`Ctrl + '`) e execute:

```bash
mvn clean install
```

Isso irá:
- Baixar todas as dependências
- Compilar o código
- Criar o arquivo JAR em `target/`

### Passo 3: Executar a aplicação

```bash
mvn spring-boot:run
```

**OU**

```bash
java -jar target/link-shortener-1.0.0.jar
```

### Passo 4: Acessar a aplicação

Abra seu navegador em:
- **Frontend:** http://localhost:8080
- **API Health:** http://localhost:8080/api/health
- **H2 Console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:linkdb`
  - Username: `sa`
  - Password: (deixe em branco)

---

## ▶️ EXECUTANDO NO VSCODE (Opção 2 - Extensão Spring Boot)

### Passo 1: Instalar extensões

Instale estas extensões no VSCode:
1. Extension Pack for Java
2. Spring Boot Extension Pack

### Passo 2: Abrir o projeto

```bash
cd D:\Estudo\Java\LinkEncurterProd
code .
```

### Passo 3: Executar usando botão de Run

1. Abra `LinkShortenerApplication.java`
2. Clique no botão ▶️ (Run) que aparece acima do método `main`
3. OU pressione `F5`

### Passo 4: Ver logs

Os logs aparecerão no painel "Terminal" ou "Debug Console"

---

## 🧪 TESTANDO A APLICAÇÃO

### Teste 1: Criar um link curto

1. Acesse http://localhost:8080
2. Cole uma URL: `https://www.google.com/search?q=spring+boot`
3. Clique em "Encurtar Link"
4. Copie o link curto gerado

### Teste 2: Usar o link curto

1. Cole o link curto em outra aba do navegador
2. Você será redirecionado para a URL original
3. O contador de cliques será incrementado

### Teste 3: Ver estatísticas

1. Na página inicial, após criar um link
2. Clique em "📊 Ver Estatísticas"
3. Veja a contagem de cliques atualizada

### Teste 4: API com cURL ou Postman

**Criar link:**
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d "{\"url\": \"https://github.com\", \"expirationHours\": 24}"
```

**Ver estatísticas:**
```bash
curl http://localhost:8080/api/stats/ABC123
```

---

## 🗄️ ACESSANDO O BANCO DE DADOS H2

1. Acesse: http://localhost:8080/h2-console
2. Configure:
   - JDBC URL: `jdbc:h2:mem:linkdb`
   - Username: `sa`
   - Password: (vazio)
3. Clique em "Connect"
4. Execute queries SQL:

```sql
-- Ver todos os links
SELECT * FROM LINKS;

-- Ver links ativos
SELECT * FROM LINKS WHERE ACTIVE = TRUE;

-- Ver links mais clicados
SELECT * FROM LINKS ORDER BY CLICK_COUNT DESC;
```

---

## 🛑 PARANDO A APLICAÇÃO

- Se executou via terminal: `Ctrl + C`
- Se executou via VSCode debugger: Clique no botão "Stop" (quadrado vermelho)

---

## 📦 PREPARANDO PARA DEPLOY NO VERCEL

### Passo 1: Instalar Git (se não tiver)

Download: https://git-scm.com/download/win

### Passo 2: Inicializar repositório Git

```bash
cd D:\Estudo\Java\LinkEncurterProd
git init
git add .
git commit -m "Initial commit - Link Shortener com Spring Boot"
```

### Passo 3: Criar repositório no GitHub

1. Acesse: https://github.com/new
2. Nome: `link-shortener`
3. Clique em "Create repository"

### Passo 4: Push para GitHub

```bash
git remote add origin https://github.com/SEU_USUARIO/link-shortener.git
git branch -M main
git push -u origin main
```

### Passo 5: Deploy na Vercel

1. Acesse: https://vercel.com
2. Faça login com GitHub
3. Clique em "New Project"
4. Importe o repositório `link-shortener`
5. Configure:
   - **Framework Preset:** Other
   - **Root Directory:** ./
   - **Build Command:** `mvn clean package`
6. Adicione variável de ambiente:
   - `BASE_URL`: sua URL do Vercel (ex: `https://seu-app.vercel.app`)
7. Clique em "Deploy"

---

## ⚙️ CONFIGURAÇÕES IMPORTANTES

### application.properties (Desenvolvimento)

Localização: `src/main/resources/application.properties`

```properties
# Porta do servidor
server.port=8080

# URL base (localhost)
app.base-url=http://localhost:8080

# Expiração padrão (horas)
app.link-expiration-hours=24

# Tamanho do código curto
app.short-code-length=6

# Domínios bloqueados
app.blocked-domains=localhost,127.0.0.1,0.0.0.0
```

### application-production.properties (Produção)

```properties
# URL base (Vercel)
app.base-url=${BASE_URL:https://your-app.vercel.app}

# Console H2 desabilitado
spring.h2.console.enabled=false
```

---

## 🔧 TROUBLESHOOTING

### Problema: Maven não encontrado

**Solução:**
1. Instale o Maven (veja seção de instalação)
2. Adicione ao PATH
3. Reinicie o terminal

### Problema: Porta 8080 já está em uso

**Solução 1:** Mude a porta em `application.properties`:
```properties
server.port=8081
```

**Solução 2:** Mate o processo usando a porta:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Problema: Erro ao compilar (dependências)

**Solução:**
```bash
mvn clean install -U
```

### Problema: Java não encontrado

**Solução:**
1. Instale Java 17+
2. Configure JAVA_HOME
3. Adicione ao PATH

### Problema: Frontend não carrega

**Solução:**
- Verifique se os arquivos estão em `src/main/resources/static/`
- Limpe o cache do navegador (`Ctrl + Shift + Delete`)
- Acesse: http://localhost:8080/index.html

---

## 📊 ENDPOINTS DA API

### POST /api/shorten
Cria um link curto

**Request:**
```json
{
  "url": "https://exemplo.com/pagina-muito-longa",
  "expirationHours": 24
}
```

**Response:**
```json
{
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://exemplo.com/pagina-muito-longa",
  "clickCount": 0,
  "createdAt": "2025-10-04T10:30:00",
  "expiresAt": "2025-10-05T10:30:00",
  "active": true
}
```

### GET /{shortCode}
Redireciona para URL original

### GET /api/stats/{shortCode}
Retorna estatísticas do link

### GET /api/health
Verifica se API está online

---

## 📝 LOGS E DEBUGGING

### Ver logs detalhados

Em `application.properties`:
```properties
logging.level.root=DEBUG
logging.level.com.linkshortener=DEBUG
spring.jpa.show-sql=true
```

### Debugar no VSCode

1. Coloque breakpoints (clique na margem esquerda do código)
2. Pressione `F5` para iniciar em modo debug
3. Use os controles de debug (F10, F11, etc.)

---

## 🔒 SEGURANÇA

⚠️ **IMPORTANTE:** Antes de colocar em produção, leia o arquivo `RELATORIO_SEGURANCA.md`

Principais pontos:
- Implemente rate limiting
- Configure HTTPS
- Restrinja CORS
- Use banco de dados persistente
- Adicione autenticação

---

## 📚 RECURSOS ADICIONAIS

- **Spring Boot Docs:** https://spring.io/projects/spring-boot
- **Spring Data JPA:** https://spring.io/projects/spring-data-jpa
- **H2 Database:** https://www.h2database.com/
- **Maven Guide:** https://maven.apache.org/guides/

---

## 🆘 SUPORTE

Se encontrar problemas:

1. Verifique os logs no terminal
2. Consulte o `RELATORIO_SEGURANCA.md` para questões de segurança
3. Verifique a estrutura de pastas
4. Confirme que todas as dependências foram baixadas

---

## ✅ CHECKLIST DE EXECUÇÃO

- [ ] Java 17+ instalado
- [ ] Maven instalado e no PATH
- [ ] VSCode instalado com extensões Java
- [ ] Projeto aberto no VSCode
- [ ] `mvn clean install` executado com sucesso
- [ ] Aplicação rodando em http://localhost:8080
- [ ] Frontend acessível
- [ ] API respondendo em /api/health
- [ ] H2 Console acessível
- [ ] Testado criação de link
- [ ] Testado redirecionamento
- [ ] Testado estatísticas

---

## 🎯 PRÓXIMOS PASSOS

1. ✅ Testar localmente
2. ⏭️ Criar repositório Git
3. ⏭️ Push para GitHub
4. ⏭️ Deploy na Vercel
5. ⏭️ Implementar melhorias de segurança
6. ⏭️ Adicionar autenticação
7. ⏭️ Migrar para banco de dados persistente

---

**Boa sorte! 🚀**

Criado por: Claude Code Assistant
Versão: 1.0
Data: 2025-10-04
