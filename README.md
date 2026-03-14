<div align="center">
  <h1 align="center">Vaulty 🔐</h1>
  
  <p align="center">
    <strong>A secure, full-stack password manager web application built with Spring Boot, featuring AES-256 encryption and user authentication.</strong>
  </p>

  <p align="center">
    <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
    <img src="https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot 3" />
    <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white" alt="Spring Security" />
    <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white" alt="Thymeleaf" />
    <img src="https://img.shields.io/badge/H2_Database-0000CC?style=for-the-badge" alt="H2 Database" />
  </p>
</div>

---

## 📖 About this Project

**Vaulty** is a personal password manager built with a robust Java Spring Boot backend. All stored passwords are encrypted using **AES-256/CBC** with a random Initialization Vector (IV) prepended to each ciphertext, ensuring strong, industry-standard security. Users can register, log in, and manage their saved passwords through a clean, Thymeleaf-powered web interface.

## 🔒 Security Architecture

*   **AES-256/CBC Encryption** with `PKCS5Padding` — every password is encrypted before being stored.
*   **Random IV per entry** — a new `SecureRandom` IV is generated for every encryption, preventing pattern attacks.
*   **Spring Security** — handles authentication, session management, and route protection.
*   **BCrypt** — user passwords are hashed before being persisted to the database.

## 🚀 Key Features

*   **User Authentication:** Secure sign-up and login with session management via Spring Security.
*   **Password CRUD:** Add, view, and manage saved passwords for different websites/services.
*   **AES Encryption Layer:** All credentials stored encrypted; decrypted only on-demand.
*   **User Profile Page:** View and manage your account details.
*   **Landing & About Pages:** Clean onboarding flow for new users.

## 🛠️ Built With

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Security | Spring Security |
| Templating | Thymeleaf |
| Database | H2 (In-Memory / File) |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| Testing | JUnit 4 |

## 💻 Getting Started

### Prerequisites

*   **Java 21** or higher
*   **Maven 3.x**

### Installation & Usage

1.  Clone the repository:
    ```bash
    git clone https://github.com/laksh1411/vaulty1.git
    cd vaulty1
    ```
2.  Configure your encryption key in `src/main/resources/application.properties`:
    ```properties
    encryption.key=your-32-char-secret-key-here!!
    ```
3.  Build and run the application:
    ```bash
    mvn spring-boot:run
    ```
4.  Open your browser and navigate to:
    ```
    http://localhost:8080
    ```

## 📁 Project Structure

```
src/main/java/com/example/passwordmanager/
├── config/
│   ├── EncryptionConfig.java     # Encryption bean configuration
│   └── SecurityConfig.java       # Spring Security configuration
├── controller/
│   ├── AuthController.java       # Login / Signup endpoints
│   ├── PasswordController.java   # Password CRUD endpoints
│   └── PageController.java       # Static page routing
├── model/
│   ├── User.java                 # User entity
│   └── PasswordEntry.java        # Password entry entity
├── repository/
│   ├── UserRepository.java
│   └── PasswordRepository.java
└── service/
    ├── EncryptionService.java    # AES-256/CBC encrypt & decrypt
    ├── PasswordService.java      # Business logic for passwords
    └── CustomUserDetailsService.java
```

## 🤝 Contributing

Contributions are welcome! Ideas for improvement:
- Migrate to a persistent database (PostgreSQL/MySQL)
- Add 2FA / OTP support
- Browser extension integration

## 📬 Contact

**Laksh Saluja**
*   [LinkedIn](https://www.linkedin.com/in/laksh14/)
*   [GitHub](https://github.com/laksh1411)
