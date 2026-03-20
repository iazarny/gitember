## 🗃️ Secret Storage Patterns by Language & Config Type

To build a robust detector, categorize secrets by file format and common key patterns across ecosystems.

---

### Java / Kotlin (`.properties`)
**Files:**  
`application.properties`, `bootstrap.properties`, `local.properties`

**Sensitive keys:**
- `spring.datasource.password`
- `mail.password`
- `okta.oauth2.client-secret`
- `secret.key`
- `google.cloud.api-key`
- `server.ssl.key-store-password`

---

### YAML
**Files:**  
`application.yml`, `values.yaml`, `docker-compose.yml`

**Sensitive keys:**
- `password`
- `token`
- `api-key`
- `client-secret`
- `db_pass`
- `private_key`
- `credential`

---

### Python (`.env`, dotenv)
**Files:**  
`.env`, `.flaskenv`, `.env.shared`

**Sensitive keys:**
- `DATABASE_URL`
- `SECRET_KEY`
- `AWS_SECRET_ACCESS_KEY`
- `FLASK_SECRET`
- `DJANGO_SETTINGS_MODULE` *(if pointing to prod)*
- `PWD`, `PASS`

---

### INI / CFG
**Files:**  
`settings.ini`, `config.cfg`, `setup.cfg`

**Sensitive keys:**
- `[auth]`, `[database]`
- `access_token`
- `client_id`
- `consumer_secret`

---

### JS / TS / Node (JSON)
**Files:**  
`package.json`, `config.json`, `auth.json`

**Sensitive keys:**
- `auth_token`
- `private_key`
- `sessionSecret`
- `stripe_key`
- `firebase_config.apiKey`

---

### JS / TS (Code)
**Files:**  
`config.js`, `constants.ts`, `env.ts`

**Patterns:**
- `const apiKey = ...`
- `export const TOKEN = ...`
- `process.env.SECRET`

---

### Go (TOML)
**Files:**  
`config.toml`, `Gopkg.toml`

**Sensitive keys:**
- `[database] pass`
- `[api] key`
- `secret`
- `access_token`

---

### PHP
**Files:**  
`config.php`, `wp-config.php`, `.env`

**Sensitive keys:**
- `DB_PASSWORD`
- `AUTH_KEY`
- `SECURE_AUTH_KEY`
- `LOGGED_IN_SALT`

---

### Ruby (YAML)
**Files:**  
`database.yml`, `secrets.yml`

**Sensitive keys:**
- `password`
- `secret_key_base`
- `devise_token`
- `access_key`

---

### C# / .NET
**Files:**  
`web.config`, `app.config`, `machine.config`, `appsettings.json`

**Sensitive keys:**
- `ConnectionString`
- `AppSecrets`
- `EncryptedPassword`
- `ClientSecret`
- `JwtSettings:Secret`

---

### Rust (TOML)
**Files:**  
`Cargo.toml`, `Rocket.toml`

**Sensitive keys:**
- `secret_key`
- `database_url`
- `tls.key`

---

## 📡 Connection String Patterns (Multi-Language)

These often embed credentials directly in a single string:

- `postgres://user:password@localhost:5432/dbname`
- `mongodb+srv://admin:password@cluster.mongodb.net`
- `redis://:password@localhost:6379`
- `jdbc:mysql://user:password@localhost:3306/db?useSSL=true`
- `amqp://user:password@localhost:5672`

---

## 🛠️ Common Key Naming Conventions

Use case-insensitive matching for these substrings:

### Authentication
- `api_key`
- `auth_token`
- `access_token`
- `session_id`

### Security
- `secret`
- `private_key`
- `salt`
- `encryption_key`

### Database
- `pwd`
- `password`
- `passwd`
- `db_pass`

### Cloud
- `aws_key`
- `gcp_token`
- `azure_secret`

---

## 🔐 Secret Value Patterns (Prefixes, Lengths, and Formats)

Beyond key names, many secrets follow **recognizable formats** — fixed prefixes, lengths, or encodings.  
These are extremely useful for high-confidence detection.

---

## 🧬 GitHub

- **Personal Access Token (classic):**  
  Prefix: `ghp_`  
  Length: ~40 chars

- **Fine-grained PAT:**  
  Prefix: `github_pat_`  
  Length: ~90+ chars

- **OAuth Access Token:**  
  Prefix: `gho_`

- **GitHub App Token:**  
  Prefix: `ghu_`

- **Refresh Token:**  
  Prefix: `ghr_`

---

## ☁️ AWS

- **Access Key ID:**  
  Prefix: `AKIA`, `ASIA`  
  Length: 20 chars  
  Pattern: `[A-Z0-9]{20}`

- **Secret Access Key:**  
  Length: 40 chars  
  Base64-like: `[A-Za-z0-9/+=]{40}`

---

## ☁️ Google Cloud (GCP)

- **API Key:**  
  Prefix: `AIza`  
  Length: ~39 chars

- **OAuth Access Token:**  
  Prefix: `ya29.`

- **Service Account (JSON):**  
  Key field: `"private_key": "-----BEGIN PRIVATE KEY-----"`

---

## ☁️ Azure

- **Client Secret:**  
  No strict prefix  
  Length: ~32–48 chars (high entropy)

- **Storage Account Key:**  
  Base64 string (~88 chars)

---

## 💳 Stripe

- **Secret Key:**  
  Prefix: `sk_live_`, `sk_test_`

- **Publishable Key:**  
  Prefix: `pk_live_`, `pk_test_`

- **Webhook Secret:**  
  Prefix: `whsec_`

---

## 💬 Slack

- **Bot/User Token:**  
  Prefix: `xoxb-`, `xoxp-`, `xoxa-`

- **Webhook URL:**  
  Pattern:  
  `https://hooks.slack.com/services/...`

---

## 🔐 JWT (JSON Web Token)

- Structure: `xxxxx.yyyyy.zzzzz` (3 Base64URL parts)
- Regex:  
  `[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+`

---

## 🔑 SSH / Private Keys

- **PEM Headers:**
    - `-----BEGIN RSA PRIVATE KEY-----`
    - `-----BEGIN OPENSSH PRIVATE KEY-----`
    - `-----BEGIN EC PRIVATE KEY-----`

---

## 🪪 Generic High-Entropy Secrets

- Long random strings (≥32 chars)
- Base64 / Hex encoded

**Examples:**
- `[A-Fa-f0-9]{32,}` (hex)
- `[A-Za-z0-9+/]{40,}={0,2}` (base64)

---

## 🔗 Connection Strings (Credential Embedded)

Look for `://user:password@` patterns:

- `postgres://user:password@host:5432/db`
- `mysql://user:password@host`
- `mongodb://user:password@cluster`
- `redis://:password@host:6379`

---

## 🧠 Detection Strategy Tips

- Combine:
    - **Key name heuristics** (`password`, `secret`)
    - **Value patterns** (prefix, length, entropy)

- Assign confidence levels:
    - ✅ High: known prefix (e.g., `ghp_`, `AKIA`)
    - ⚠️ Medium: structured format (JWT, connection string)
    - ❓ Low: generic high-entropy string

- Reduce false positives:
    - Ignore test values (`test`, `example`, `dummy`)
    - Ignore short strings (<16 chars unless prefix match)

---

# Shannon Entropy (Heuristic-based)

```java

public class SecretScanner {
    // Example pattern for generic high-probability secrets
    private static final Pattern GENERIC_SECRET = 
        Pattern.compile("(?i)(password|secret|apikey|payload)\\s*[:=]\\s*[\"']([^\"']+)[\"']");

    public List<Finding> scanFile(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        List<Finding> finding = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher matcher = GENERIC_SECRET.matcher(line);
            
            while (matcher.find()) {
                String potentialSecret = matcher.group(2);
                if (calculateEntropy(potentialSecret) > 3.5) { // Threshold for randomness
                    finding.add(new Finding(path, i + 1, "High entropy secret detected"));
                }
            }
        }
        return finding;
    }

    private double calculateEntropy(String s) {
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char c : s.toCharArray()) {
            frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
        }
        double entropy = 0;
        for (int count : frequencies.values()) {
            double p = (double) count / s.length();
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }
}

```

## 🚀 Next Step

Implement detection engine layers:
1. File filter (by type / name)
2. Key-based detection
3. Value-pattern detection (this section)
4. Entropy scoring



## 🚀 Next Step

Build a recursive file scanner that:
- Traverses directories
- Respects `.gitignore`
- Applies pattern matching rules  
