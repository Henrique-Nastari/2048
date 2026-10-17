# 2048 Classic

Jogo 2048 feito em **Kotlin Multiplatform + Compose Multiplatform**, pronto para publicar na Google Play.
100% local (sem backend), com monetização via **AdMob** (banner, interstitial e rewarded) e **compra única "remove ads"** via **Play Billing**.

- `applicationId`: `com.hcn2048.game`
- Nome exibido: **2048 Classic**
- Idiomas: inglês (padrão) + português (Brasil)
- Android: `minSdk 24` / `targetSdk 37`

---

## Stack (custo zero)

| Camada | Tecnologia | Custo |
|---|---|---|
| UI | Compose Multiplatform 1.12 (Material3) | R$ 0 |
| Lógica do jogo | Kotlin puro em `commonMain` (testável, pronta para iOS) | R$ 0 |
| Estado | `StateFlow` + ViewModel próprio + Coroutines | R$ 0 |
| Persistência | DataStore Preferences (recordes, partida salva, tema, IAP) | R$ 0 |
| Injeção de dependência | Koin | R$ 0 |
| Anúncios | AdMob SDK (banner + interstitial + rewarded) + UMP (consentimento LGPD/GDPR) | R$ 0 (você recebe) |
| Compras | Play Billing 9 (`remove_ads`, não-consumível) | R$ 0 |
| Review in-app | Play In-App Review | R$ 0 |
| CI | GitHub Actions | R$ 0 (2.000 min/mês) |
| Loja | Google Play Console | US$ 25 (taxa única) |

**Sem servidor, sem Firebase, sem assinatura mensal.** Lucro líquido = receita − R$ 0.

---

## Estrutura

```
HCN2048_ktm/
├── androidApp/                     # App Android (entry point)
│   └── src/main/kotlin/com/hcn2048/game/
│       ├── Hcn2048Application.kt   # Inicia Koin + AdMob + consentimento
│       └── MainActivity.kt         # setContent { App() }
├── shared/                         # Código compartilhado (KMP)
│   └── src/
│       ├── commonMain/kotlin/com/hcn2048/
│       │   ├── model/              # Tile, GameBoard, Direction...
│       │   ├── engine/             # GameEngine (lógica pura) + GameSerializer
│       │   ├── data/               # PreferencesRepository (interface)
│       │   ├── ads/                # AdManager (interface) + AdBanner (expect)
│       │   ├── billing/            # BillingManager (interface)
│       │   ├── review/             # ReviewPrompt (interface)
│       │   ├── ui/                 # GameViewModel, GameScreen, componentes, temas
│       │   ├── di/                 # sharedModule (Koin)
│       │   └── App.kt              # Composable raiz
│       ├── androidMain/kotlin/com/hcn2048/   # Implementações Android
│       │   ├── ads/                # AdManagerImpl, AdsConsent (UMP), AdBanner
│       │   ├── billing/            # BillingManagerImpl (Play Billing 9)
│       │   ├── data/               # PreferencesRepositoryImpl (DataStore)
│       │   └── review/             # ReviewPromptImpl
│       ├── commonMain/composeResources/      # strings EN + PT-BR
│       └── commonTest/             # Testes do engine e do serializador
└── iosApp/                         # Entry point iOS (para o futuro)
```

---

## Como rodar

**Android Studio:** abra a pasta do projeto e rode a configuração `androidApp`.

**Linha de comando:**

```bash
./gradlew :androidApp:assembleDebug        # APK de debug
./gradlew :shared:testAndroidHostTest      # testes do engine (rápidos, na JVM)
./gradlew :androidApp:assembleRelease      # AAB/APK de release (R8 ativo)
```

O APK de debug sai em `androidApp/build/outputs/apk/debug/`.

---

## Monetização embutida no jogo

| Gatilho | Formato | Regra no código |
|---|---|---|
| Rodapé da tela | Banner | oculto se `adsRemoved` |
| Fim de jogo | Interstitial | 1 a cada **3 game overs**, nunca no meio da partida |
| Botão "Desfazer" | Rewarded | **1º undo grátis por partida**, depois só assistindo anúncio |
| "Reviver" no Game Over | Rewarded | **1 vez por partida**: remove o menor bloco e continua |
| Ajustes > "Remover anúncios" | IAP única | desativa banner + interstitial (rewarded continua opcional) |
| Ao alcançar 2048 pela 1ª vez | Review in-app | pede avaliação no momento de dopamina |

## Configurar AdMob (produção)

1. Crie uma conta em [admob.google.com](https://admob.google.com) e registre o app (`com.hcn2048.game`).
2. Crie os blocos: **Banner**, **Interstitial** e **Rewarded**.
3. Copie os IDs para `local.properties` (veja os campos comentados no arquivo):
   `ADMOB_APP_ID`, `ADMOB_BANNER_ID`, `ADMOB_INTERSTITIAL_ID`, `ADMOB_REWARDED_ID`.
4. No AdMob, configure **Privacidade e mensagens** (GDPR + ID de publicidade) — o app já usa o SDK UMP para exibir os formulários.

> Sem esses IDs, o app usa os **IDs de teste oficiais do Google** — seguro para desenvolvimento.

## Configurar Play Billing

1. Assine o app no Play Console (Internal testing já serve para testar).
2. **Monetizar > Produtos > Produtos no app** → crie o produto com **ID exatamente `remove_ads`** (produto único, não consumível), preço sugerido R$ 4,90.
3. Adicione sua conta Google como **licenciada para testes** (Configurações > Testes de licença).
4. O valor exibido no diálogo vem da própria Play Store (localizado).

## Assinar o release

```bash
keytool -genkeypair -v -keystore hcn2048-upload.jks -alias hcn2048 \
  -keyalg RSA -keysize 2048 -validity 10000
```

Depois preencha `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
`RELEASE_KEY_PASSWORD` em `local.properties` (campos comentados no arquivo).
Com isso, `./gradlew :androidApp:bundleRelease` gera o `.aab` já assinado em
`androidApp/build/outputs/bundle/release/`.

> ⚠️ Guarde o `.jks` e as senhas em local seguro (e fora do Git). Perder a chave = perder a
> possibilidade de atualizar o app para sempre.

## Publicar na Play Store (checklist)

- [ ] Conta de dev no Play Console (US$ 25, única)
- [ ] Criar o app com o nome **2048 Classic**
- [ ] Política de privacidade publicada (obrigatória com anúncios) — pode usar gerador (ex.: freeprivacypolicy.com) ou GitHub Pages, citando AdMob/Play Billing/DataStore
- [ ] Preencher "Segurança dos dados" (Data safety): coleta de ID de publicidade pelo AdMob, dados salvos localmente
- [ ] Classificação de conteúdo + público-alvo
- [ ] Ícone 512×512, feature graphic 1024×500 e screenshots (celular)
- [ ] Produto `remove_ads` criado e testado com license tester
- [ ] `bundleRelease` → upload em **Teste interno** → validar → Teste fechado → Produção
- [ ] (Recomendado) Testar os anúncios de teste no emulador/dispositivo antes de trocar para IDs reais

## Git e segredos (o que NUNCA versionar)

O `.gitignore` já protege os arquivos sensíveis deste projeto:

| Arquivo | O que contém | Status |
|---|---|---|
| `local.properties` | caminho do SDK, IDs do AdMob, senhas da keystore | **ignorado** |
| `*.jks` / `*.keystore` | chave de assinatura do app (**nunca** vai ao Git) | **ignorado** |
| `keystore.properties`, `release.jks` | credenciais usadas no CI | **ignorado** |
| `build/`, `.gradle/`, `.idea/`, `.kotlin/` | artefatos de build e configs da IDE | **ignorado** |
| `*.apk` / `*.aab` | binários gerados | **ignorado** |

O template `local.properties.example` documenta todas as chaves necessárias
(sem valores reais) e pode ser versionado com segurança.

**Antes do primeiro push, confirme que nada sensível entrou:**

```bash
git status                                # local.properties NÃO deve aparecer
git check-ignore -v local.properties      # deve mostrar a regra do .gitignore
git ls-files | findstr local.properties   # Windows: deve vir vazio
```

**Se algo vazar por acidente**, trate como comprometido (apagar o commit não basta):
- **Keystore + senhas**: gere uma nova chave e solicite ao Google a redefinição da
  chave de upload (Play Console → Configurações → Integridade do app).
- **Service account / token do Play Console**: revogue no Google Cloud e gere outro.
- **IDs do AdMob**: não são críticos (vão embutidos no APK), mas mantenha fora do
  repositório para evitar clones com seus IDs e métricas falsas.

**No CI (GitHub Actions)**, os segredos vão em *Settings → Secrets and variables → Actions*:

| Secret | Valor |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | conteúdo do `.jks` em base64 |
| `RELEASE_STORE_PASSWORD` | senha da keystore |
| `RELEASE_KEY_ALIAS` | alias da chave (`hcn2048`) |
| `RELEASE_KEY_PASSWORD` | senha da chave |

Gerar o base64 (PowerShell) e copiar para a área de transferência:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:USERPROFILE\.android-keys\hcn2048-upload.jks")) | Set-Clipboard
```

## Roadmap sugerido

1. Wiring iOS no `iosApp` (o Compose já roda no shared; falta só iniciar Koin no entry point Swift).
2. Sons CC0 (Kenney.nl) + SoundPool.
3. App Open Ads (ao abrir o app) e banner adaptativo.
4. Firebase Crashlytics/Analytics (grátis) para monitorar crashes e funil de compras.
5. Remote Config para testar preço do `remove_ads` e frequência do interstitial sem atualizar o app.
6. Mais temas e skins como novo gatilho de rewarded.
