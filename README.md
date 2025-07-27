# Budget Tracker

A simple and intuitive Android application to help you manage your personal finances, track your expenses, and stay on top of your budget.

## Features

- **Monthly Budgeting**: Set a total monthly budget and define category-specific limits to keep your spending in check.
- **Expense Tracking**: Easily record your daily expenses with details such as date, category, amount, and an optional description.
- **User-Defined Categories**: Create, edit, and delete your own expense categories to tailor the app to your needs.
- **Monthly Summary**: Get a clear overview of your financial health with a monthly summary that shows your budget, expenses, and the difference for each category.
- **Data Export/Import**: Back up your financial data by exporting it to a JSON file, and restore it whenever you need to.
- **Month and Year Selection**: Navigate through your financial history by easily selecting the month and year you want to review.
- **Persistent Storage**: All your data is stored locally on your device, ensuring privacy and offline access.

## Getting Started

To get a local copy up and running, follow these simple steps:

1.  **Clone the repository**:
    ```sh
    git clone git@github.com:rifatr/Budget.git
    ```
2.  **Open in Android Studio**:
    -   Launch Android Studio and select "Open an existing Android Studio project".
    -   Navigate to the cloned repository and click "OK".
3.  **Build and Run**:
    -   Let Android Studio sync the project and download the required dependencies.
    -   Click the "Run" button or press `Shift`+`F10` to build and run the app on an emulator or a physical device.

## Technologies Used

- **Kotlin**: The primary programming language for building modern Android apps.
- **Jetpack Compose**: A modern toolkit for building native Android UI.
- **Room Persistence Library**: Provides an abstraction layer over SQLite to allow for more robust database access.
- **MVVM Architecture**: The Model-View-ViewModel architectural pattern is used to separate the business logic from the UI.
- **Navigation Compose**: A framework for navigating between composables while taking advantage of the Navigation component's infrastructure and features.
- **Gson**: A Java library that can be used to convert Java Objects into their JSON representation and vice-versa.
- **Gradle**: The build automation tool used for building Android applications.
