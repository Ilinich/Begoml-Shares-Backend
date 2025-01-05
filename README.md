## Begoml Shares:
An application for buying and selling stocks. Users have their own wallet where they can deposit funds, which can then be used to purchase stocks, as well as withdraw money back from the wallet. Additionally, users can maintain a personal portfolio with stocks from various companies or cryptocurrencies.

## Architecture explained:
This application architecture is designed to align with CLEAN principles, focusing on separating each layer with a distinct purpose to facilitate unit testing for each layer independently.

To implement this architecture, I'm using Koin as the dependency injection framework: [Koin](https://insert-koin.io/)

To enhance testability, all Controllers and APIs adhere to interfaces, which are injected as dependencies wherever they are used, rather than injecting specific implementations. This approach helps decouple components and allows each layer to be easily mocked and tested in isolation.

### App layer:
This file defines the application's main method, where Koin setup and initial configuration are performed. Here, dependencies, environment settings, and initial parameters are specified to enable the backend service to run properly.

### Presentation layer:
The layer responsible for handling client-facing data and processing incoming requests. This module contains controllers and routes that manage HTTP requests and generate responses, linking user requests with business logic.

### Logic layer:
The business logic layer where core application rules and operations are managed. 
This is where interfaces, services, use-cases, and overall business processes are defined. This layer separates business logic from implementation details, making the project easier to test and maintain.

### Data layer:
The data layer, responsible for handling interactions with external sources, such as databases and APIs. 
This module contains repository implementations, data models, and any adapters needed to transform data between layers.
It ensures data access and management while isolating it from business logic.


### Redis Integration:
In addition to PostgreSQL for persistent data storage, Redis is used to implement rate-limiting and caching mechanisms within the application.
Rate-Limiting: Redis is used to store and manage the rate limits for various user actions, such as deposits, withdrawals, and requests. By using Redis, we can efficiently track the number of requests made by a user in a specified time frame (e.g., a day). If a user exceeds the predefined limit (e.g., 10 actions per day), further requests are blocked, preventing abuse and ensuring fair usage.
Caching: Redis is also used as an in-memory cache for frequently accessed data, such as user wallet balances and stock prices. This helps reduce the load on the PostgreSQL database and improves the overall performance of the application.
Redis is configured for persistence through RDB snapshots, ensuring that data is periodically written to disk for durability. In the event of a server restart, the data can be restored from these snapshots.

### Database (PostgreSQL):
PostgreSQL is used as the primary database for storing user data, transactions, stock portfolio details, and more.
User Wallets: Each user has a wallet in PostgreSQL where their balance, transaction history, and other financial data are stored. The database ensures durability and consistency of the financial records, and it provides complex querying capabilities for reporting and auditing.
Transactions: All stock transactions (deposits, withdrawals, purchases) are logged in PostgreSQL, ensuring traceability and accountability. The database is also used to maintain user portfolios and related metadata.

PostgreSQL is configured with a robust schema for handling relationships between users, transactions, and stocks, ensuring data integrity and scalability for the growing application.