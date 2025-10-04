# 🚂 Railway Deployment Guide - LinkShort

Railway é a melhor opção para Spring Boot + PostgreSQL!

---

## Step 1: Prepare o Projeto

Primeiro, vamos criar alguns arquivos necessários.

### 1.1 - Criar `Procfile` (Railway usa isso)

Já vou criar para você automaticamente.

### 1.2 - Commit e Push para GitHub

```bash
git add .
git commit -m "Prepare for Railway deployment"
git push origin main
```

---

## Step 2: Criar Conta no Railway

1. Acesse: https://railway.app
2. Click **Login**
3. Escolha **Login with GitHub**
4. Autorize o Railway

✅ Você ganha **$5 de crédito grátis por mês** (suficiente para o projeto!)

---

## Step 3: Criar Novo Projeto

1. No dashboard do Railway, click **New Project**
2. Escolha **Deploy from GitHub repo**
3. Se perguntarem permissão, click **Configure GitHub App**
4. Selecione seu repositório: `LinkShortner`
5. Click **Deploy Now**

⏳ Railway vai detectar que é Java e começar o build...

---

## Step 4: Adicionar PostgreSQL Database

1. No seu projeto Railway, click **+ New**
2. Selecione **Database**
3. Escolha **Add PostgreSQL**
4. ✅ Database criada!

Railway automaticamente cria variáveis:
- `DATABASE_URL`
- `PGHOST`
- `PGPORT`
- `PGUSER`
- `PGPASSWORD`
- `PGDATABASE`

---

## Step 5: Configurar Variáveis de Ambiente

Railway usa `DATABASE_URL` mas Spring Boot precisa de formato diferente.

1. Click no seu **Spring Boot service** (não no database)
2. Vá para aba **Variables**
3. Click **+ New Variable**

Adicione estas variáveis:

```
SPRING_PROFILES_ACTIVE=production
POSTGRES_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
POSTGRES_USER=${{Postgres.PGUSER}}
POSTGRES_PASSWORD=${{Postgres.PGPASSWORD}}
```

**IMPORTANTE:** Railway substitui `${{Postgres.XXXX}}` automaticamente!

4. Click **Add** para cada variável

---

## Step 6: Configurar o Build

1. No serviço Spring Boot, vá para **Settings**
2. Em **Build**, configure:

**Build Command:**
```
mvn clean package -DskipTests
```

**Start Command:**
```
java -Dserver.port=$PORT -jar target/link-shortener-1.0.0.jar
```

3. Click **Save**

---

## Step 7: Deploy!

1. Vá para aba **Deployments**
2. Click **Deploy** (ou espere deploy automático)
3. Aguarde 2-5 minutos...

Você verá os logs em tempo real! ✨

---

## Step 8: Obter a URL do Projeto

1. Vá para **Settings**
2. Em **Environment**, você verá:
   - **Public Networking**
   - Click **Generate Domain**
3. Uma URL será gerada: `seu-projeto.up.railway.app`

✅ **Seu projeto está no ar!**

---

## Step 9: Testar o Deploy

Acesse sua URL gerada:
```
https://seu-projeto.up.railway.app
```

### Teste:
1. ✅ Página carrega
2. ✅ Criar um link curto
3. ✅ Ver QR Code
4. ✅ Estatísticas aparecem (números reais!)
5. ✅ Clicar no link curto redireciona

---

## Step 10: Configurar BASE_URL

Agora que você tem a URL, configure:

1. Vá para **Variables** do serviço
2. Adicione:
```
BASE_URL=https://seu-projeto.up.railway.app
```

3. Railway vai fazer redeploy automático

---

## 📊 Verificar Database

### Acessar Database:

1. Click no serviço **Postgres**
2. Vá para aba **Data**
3. Você pode executar SQL:

```sql
-- Ver todos os links
SELECT * FROM links ORDER BY created_at DESC;

-- Contar links
SELECT COUNT(*) FROM links;

-- Ver estatísticas
SELECT
  COUNT(*) as total_links,
  SUM(click_count) as total_clicks,
  COUNT(CASE WHEN active = true THEN 1 END) as active_links
FROM links;
```

---

## 💰 Custos

### Free Tier ($5 crédito/mês):

**Seu uso estimado:**
- Web Service: ~$2-3/mês
- PostgreSQL: ~$1-2/mês
- **Total: ~$3-5/mês** ✅ Cabe no free tier!

### Se ultrapassar:
- Adicione cartão de crédito
- Só paga o que usar além dos $5

### Dicas para economizar:
1. Use sleep mode quando não estiver usando
2. Monitore uso em **Usage**
3. Delete serviços não usados

---

## 🔧 Troubleshooting

### Build Falha

**Erro:** Maven not found
```bash
# Railway detecta Java automaticamente
# Se falhar, adicione em Settings > Environment:
NIXPACKS_JDK_VERSION=17
```

### Aplicação não inicia

**Check logs:**
1. Vá para **Deployments**
2. Click no deployment ativo
3. Veja os logs

**Erros comuns:**
- Porta incorreta (use `$PORT`)
- Database URL errada
- Java version mismatch

### Database connection error

**Verifique:**
1. Variáveis estão corretas
2. Database está "healthy" (verde)
3. Formato da URL: `jdbc:postgresql://...`

---

## 🚀 Deploy Automático

Cada vez que você fizer push para GitHub:

```bash
git add .
git commit -m "Update feature X"
git push origin main
```

Railway automaticamente:
1. ✅ Detecta o push
2. ✅ Builda o projeto
3. ✅ Faz deploy
4. ✅ Zero downtime!

---

## 📱 Monitore seu App

### Metrics

1. Vá para aba **Metrics**
2. Veja:
   - CPU usage
   - Memory usage
   - Network traffic

### Logs em Tempo Real

1. Vá para aba **Deployments**
2. Click **View Logs**
3. Filtros disponíveis

### Alertas (Opcional)

1. Settings → **Webhooks**
2. Adicione webhook do Discord/Slack
3. Receba notificações de deploys

---

## 🔒 Segurança em Produção

### 1. Variáveis Sensíveis

✅ Railway já protege automaticamente
- Variáveis não aparecem em logs
- Criptografadas em repouso

### 2. HTTPS

✅ Railway fornece SSL grátis automaticamente
- Todas as URLs usam HTTPS
- Certificado renovado automaticamente

### 3. Database Backups

1. Click no Postgres
2. Settings → **Backups**
3. Ative backups automáticos (opção paga)

**Free Tier:** Sem backups automáticos
**Solução:** Exporte manualmente 1x/semana:

```bash
# Conecte ao database e exporte
railway run psql $DATABASE_URL -c "\copy links to 'backup.csv' CSV HEADER"
```

---

## 🔄 Rollback (se algo der errado)

1. Vá para **Deployments**
2. Encontre o deployment que funcionava
3. Click nos 3 pontinhos (⋮)
4. **Rollback to this version**

✅ Volta para versão anterior em segundos!

---

## 📈 Scaling (se crescer muito)

### Vertical Scaling
1. Settings → **Resources**
2. Aumente RAM/CPU se necessário
3. Custo aumenta proporcionalmente

### Horizontal Scaling
Railway suporta, mas:
- Precisa configurar load balancer
- Só necessário para milhares de usuários

---

## 🎯 Checklist Final

Antes de considerar "produção":

- [ ] Deploy funcionando
- [ ] Database conectado
- [ ] Estatísticas mostram dados reais
- [ ] Links redirecionam corretamente
- [ ] QR Code funciona
- [ ] HTTPS ativo
- [ ] Custom domain (opcional)
- [ ] Backup strategy definida
- [ ] Monitore uso de créditos

---

## 📚 Links Úteis

- **Railway Docs:** https://docs.railway.app
- **Railway Discord:** https://discord.gg/railway
- **Status Page:** https://status.railway.app

---

## 🆘 Precisa de Ajuda?

1. **Railway Discord** - Comunidade muito ativa
2. **Railway Docs** - Documentação completa
3. **GitHub Issues** - Suporte oficial

---

## 🎉 Parabéns!

Seu LinkShort está rodando em produção! 🚀

**Próximos passos:**
1. Compartilhe o link com amigos
2. Monitore estatísticas
3. Adicione features se quiser

**URL do seu projeto:**
```
https://seu-projeto.up.railway.app
```

---

**Desenvolvido com ☕ Spring Boot + 🚂 Railway**
