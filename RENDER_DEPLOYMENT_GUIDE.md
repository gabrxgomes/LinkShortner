# 🚀 Render.com Deployment Guide - LinkShort

**100% Grátis • Sem Cartão de Crédito • Deploy Automático**

---

## 🎯 O que você vai conseguir:

✅ Spring Boot rodando 24/7 (com UptimeRobot)
✅ PostgreSQL grátis incluído
✅ HTTPS automático
✅ Deploy automático do GitHub
✅ **Totalmente grátis para sempre!**

---

## Step 1: Criar Conta no Render

1. Acesse: https://render.com
2. Click **Get Started**
3. Escolha **Sign Up with GitHub**
4. Autorize o Render
5. ✅ Conta criada!

**Não precisa de cartão de crédito!** 💳❌

---

## Step 2: Criar PostgreSQL Database

1. No dashboard do Render, click **New +**
2. Escolha **PostgreSQL**
3. Configure:
   - **Name:** `linkshort-db`
   - **Database:** `linkshort`
   - **User:** `linkshort_user`
   - **Region:** escolha o mais próximo (ex: Ohio/Oregon)
   - **PostgreSQL Version:** 16 (ou mais recente)
   - **Plan:** **Free** ✅
4. Click **Create Database**

⏳ Aguarde 1-2 minutos...

### Copiar Credenciais:

Quando o database estiver pronto:

1. Vá para a aba **Info**
2. Você verá:
   - **Internal Database URL** (use esta!)
   - **External Database URL**
   - **PSQL Command**

**Copie a "Internal Database URL"** - algo como:
```
postgresql://linkshort_user:XXXXX@dpg-xxxxx/linkshort
```

---

## Step 3: Criar Web Service

1. No dashboard, click **New +**
2. Escolha **Web Service**
3. Click **Build and deploy from a Git repository**
4. Click **Next**

### Conectar GitHub:

1. Click **+ Connect account** (se necessário)
2. Autorize o Render
3. Encontre seu repo: `gabrxgomes/LinkShortner`
4. Click **Connect**

---

## Step 4: Configurar o Web Service

Preencha as informações:

### **General:**
- **Name:** `linkshort` (ou o que quiser)
- **Region:** Same as database (Ohio/Oregon)
- **Branch:** `main`
- **Root Directory:** deixe vazio
- **Runtime:** `Java`

### **Build Command:**
```bash
mvn clean package -DskipTests
```

### **Start Command:**
```bash
java -Dserver.port=$PORT -jar target/link-shortener-1.0.0.jar
```

### **Plan:**
- Escolha **Free** ✅

---

## Step 5: Adicionar Environment Variables

Role para baixo até **Environment Variables**.

Click **Add Environment Variable** e adicione:

### Variable 1:
```
Key: SPRING_PROFILES_ACTIVE
Value: production
```

### Variable 2:
```
Key: POSTGRES_URL
Value: jdbc:postgresql://dpg-xxxxx/linkshort
```
⚠️ **IMPORTANTE:** Pegue a Internal Database URL que você copiou no Step 2 e adicione `jdbc:` no começo!

**Exemplo:**
- URL copiada: `postgresql://linkshort_user:abc123@dpg-xxxxx/linkshort`
- Coloque: `jdbc:postgresql://linkshort_user:abc123@dpg-xxxxx/linkshort`

### Variable 3:
```
Key: POSTGRES_USER
Value: linkshort_user
```
(ou o user que aparece na sua URL)

### Variable 4:
```
Key: POSTGRES_PASSWORD
Value: [senha da URL]
```
(pegue da URL entre `:` e `@`)

### Variable 5:
```
Key: BASE_URL
Value: https://linkshort.onrender.com
```
⚠️ Substitua `linkshort` pelo nome que você escolheu!

---

## Step 6: Deploy!

1. Click **Create Web Service**
2. ⏳ Aguarde 5-10 minutos (primeiro deploy é lento)

Você verá os logs em tempo real:
```
==> Downloading dependencies...
==> Building...
==> Starting service...
Started LinkShortenerApplication in XX seconds
```

✅ **Quando aparecer "Started", está no ar!**

---

## Step 7: Acessar seu App

Sua URL será:
```
https://seu-nome.onrender.com
```

Teste:
1. ✅ Abra a URL
2. ✅ Crie um link
3. ✅ Veja QR Code
4. ✅ Teste redirecionamento
5. ✅ Veja estatísticas

---

## Step 8: Configurar UptimeRobot (Manter 24/7 acordado)

O Render **dorme após 15 minutos de inatividade** no free tier.
**Solução:** UptimeRobot faz ping a cada 5 minutos = sempre acordado!

### 8.1 - Criar Conta

1. Acesse: https://uptimerobot.com
2. Click **Register for FREE**
3. Preencha email e senha
4. Confirme email
5. ✅ Conta criada!

### 8.2 - Adicionar Monitor

1. No dashboard, click **+ Add New Monitor**
2. Configure:
   - **Monitor Type:** HTTP(s)
   - **Friendly Name:** LinkShort
   - **URL:** `https://seu-app.onrender.com/api/health`
   - **Monitoring Interval:** 5 minutes
3. Click **Create Monitor**

✅ **Pronto!** Agora seu app nunca vai dormir!

### Como funciona:
- A cada 5 minutos, UptimeRobot faz request em `/api/health`
- Render vê atividade e mantém o app acordado
- **Totalmente grátis e legal!** 👍

---

## 🎉 Seu App Está no Ar 24/7!

Acesse: `https://seu-app.onrender.com`

---

## 📊 Verificar Database

### Acessar PSQL:

1. No Render, vá para seu **PostgreSQL database**
2. Aba **Shell**
3. Click **PSQL Command** → vai abrir um terminal

Execute queries:

```sql
-- Ver tabelas
\dt

-- Ver todos os links
SELECT * FROM links ORDER BY created_at DESC LIMIT 10;

-- Estatísticas
SELECT
  COUNT(*) as total_links,
  SUM(click_count) as total_clicks,
  COUNT(CASE WHEN active = true AND expires_at > NOW() THEN 1 END) as active_links
FROM links;

-- Ver link específico
SELECT * FROM links WHERE short_code = 'abc123';
```

---

## 🔧 Troubleshooting

### Build Falha

**Erro:** "Could not find or load main class"
- Verifique se o Start Command está correto
- JAR name deve ser: `link-shortener-1.0.0.jar`

**Erro:** "Port already in use"
- Use `$PORT` no start command (Render injeta automaticamente)

### Database Connection Error

**Erro:** "Connection refused"
- Verifique se a POSTGRES_URL está correta
- Deve começar com `jdbc:postgresql://`
- Use **Internal Database URL**, não External

**Erro:** "password authentication failed"
- Verifique POSTGRES_USER e POSTGRES_PASSWORD
- Pegue da Internal Database URL

### App está lento

- Normal no free tier
- Primeiro request pode demorar (cold start)
- UptimeRobot resolve isso

### UptimeRobot não funciona

- Verifique se a URL está correta
- Teste manualmente: `https://seu-app.onrender.com/api/health`
- Deve retornar: `{"status":"UP"}`

---

## 📈 Monitoramento

### Ver Logs:

1. No Render, vá para seu **Web Service**
2. Aba **Logs**
3. Logs em tempo real

### Métricas:

1. Aba **Metrics**
2. Veja:
   - CPU usage
   - Memory usage
   - Bandwidth

### UptimeRobot Status:

1. Dashboard do UptimeRobot
2. Veja uptime % (deve ser ~99-100%)
3. Histórico de downtime

---

## 🔄 Deploy Automático

Cada push para GitHub = deploy automático!

```bash
# Faça mudanças no código
git add .
git commit -m "Nova feature"
git push origin main
```

Render automaticamente:
1. ✅ Detecta o push
2. ✅ Builda
3. ✅ Faz deploy
4. ⏳ ~3-5 minutos

---

## 💾 Backup do Database

### Manual (Recomendado semanalmente):

1. No database do Render, aba **Shell**
2. Execute:
```bash
pg_dump $DATABASE_URL > backup.sql
```

### Download backup:

No Render não tem UI para download.

**Alternativa:**
- Conecte com DBeaver/pgAdmin usando External URL
- Export manual

---

## 🔒 Segurança

### ✅ Já configurado:

- HTTPS automático (SSL grátis)
- Environment variables seguras
- Database em rede privada (Internal URL)

### 🛡️ Melhorias recomendadas:

1. **Restringir CORS:**
   - Edite `SecurityConfig.java`
   - Mude de `*` para seu domínio

2. **Rate Limiting:**
   - Adicione Spring Rate Limiter
   - Proteja contra spam

3. **Custom Domain (opcional):**
   - Compre domínio
   - Configure no Render (Settings → Custom Domains)

---

## 💰 Limites do Free Tier

### Render Free:
- ✅ 750 horas/mês (= 24/7 de 1 app)
- ✅ PostgreSQL: 256 MB (~ 100K links)
- ✅ 100 GB bandwidth/mês
- ⚠️ Dorme após 15min (resolvido com UptimeRobot)
- ⚠️ Build time: máx 15min
- ⚠️ Deploy time: pode ser lento

### UptimeRobot Free:
- ✅ 50 monitores
- ✅ 5 minutos interval
- ✅ Email alerts
- ✅ 2 meses de logs

---

## 📱 Extras Úteis

### Custom Domain (Grátis):

1. Compre domínio (Namecheap, GoDaddy)
2. No Render: Settings → Custom Domains
3. Adicione seu domínio
4. Configure DNS (CNAME)
5. ✅ SSL automático

### Email Alerts:

1. UptimeRobot → My Settings
2. Alert Contacts → Add Email
3. Receba notificações se cair

### Status Page (opcional):

1. UptimeRobot → Public Status Pages
2. Crie página pública
3. Compartilhe: `https://status.seu-site.com`

---

## 🚀 Performance Tips

1. **Otimize queries:**
   - Já tem indexes no `short_code`
   - Queries otimizadas

2. **Connection Pooling:**
   - Já configurado (HikariCP)
   - 5 connections max

3. **Caching (futuro):**
   - Adicione Redis se crescer
   - Cache estatísticas

---

## 📚 Links Úteis

- **Render Docs:** https://render.com/docs
- **Render Community:** https://community.render.com
- **UptimeRobot Docs:** https://uptimerobot.com/help
- **Status:** https://status.render.com

---

## 🎯 Checklist Final

- [ ] Database PostgreSQL criado
- [ ] Web Service deployado
- [ ] Environment variables configuradas
- [ ] App acessível via HTTPS
- [ ] Criar link funciona
- [ ] Redirecionamento funciona
- [ ] QR Code funciona
- [ ] Estatísticas mostram números reais
- [ ] UptimeRobot configurado
- [ ] Monitor ativo (uptime 100%)

---

## 🆘 Precisa de Ajuda?

1. **Render Community:** https://community.render.com
2. **Render Support:** https://render.com/support
3. **UptimeRobot Support:** support@uptimerobot.com

---

## 🎊 Parabéns!

Seu **LinkShort** está rodando 24/7 gratuitamente! 🎉

**URL do seu projeto:**
```
https://seu-app.onrender.com
```

**Compartilhe com amigos e adicione no portfólio!** ✨

---

**Desenvolvido com ☕ Spring Boot + 🌐 Render + 🤖 UptimeRobot**

**100% Grátis • 24/7 Online • Zero Configuração Complexa**
