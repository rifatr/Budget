# Budget Tracker

A beautiful and comprehensive Android expense tracking app built with Jetpack Compose and modern Android development practices.

## Features

### 🏠 Home Screen
- Clean, intuitive interface with monthly budget overview
- Month/year selector for navigating different periods
- Quick access to all major features
- Stunning Material 3 design with icons

### 💰 Budget Management
- Set overall monthly budget limits
- Create custom expense categories 
- Set category-wise budget limits
- Template system for reusing budgets across months
- Smart prompts when logging expenses for uncategorized items

### 📊 Expense Tracking
- Log expenses with date, category, amount, and optional description
- Editable date selection with date picker
- Input validation (amount limited to 6 digits before decimal, 2 after)
- Comprehensive expense history with search and delete functionality
- Real-time validation and error handling

### 📈 Summary & Analytics
- Monthly summary table showing budgeted vs actual expenses
- Delta calculations (remaining/overspent amounts)
- Progress indicators for each category
- Beautiful card-based layout with totals
- Visual feedback for budget performance

### ⚙️ Settings & Data Management
- **Export/Import Functionality**: 
  - Export all data to JSON format
  - Import data from JSON files
  - Perfect for backups and data migration
  - Real-time status feedback with success/error messages
  - Loading indicators during operations
- **Category Management**: Create, edit, and delete custom categories
- **Data Persistence**: All data stored locally using Room database

### 🎨 User Experience
- **Beautiful UI**: Modern Material 3 design with smooth animations
- **Input Validation**: Smart validation preventing invalid data entry
- **Error Handling**: Comprehensive error handling with user-friendly messages
- **Performance**: Optimized with proper state management and efficient database queries
- **Accessibility**: Proper content descriptions and keyboard navigation

## Technologies Used

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room with SQLite
- **Navigation**: Jetpack Compose Navigation
- **State Management**: StateFlow and Compose State
- **Data Serialization**: Gson for JSON export/import
- **Dependency Injection**: Manual DI with Application class
- **Build System**: Gradle with Kotlin DSL

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 or higher
- Kotlin 1.9.22

### Installation
1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```

2. Open the project in Android Studio

3. Sync the project and let Gradle download dependencies

4. Run the app on an emulator or physical device

### Building
To build the debug APK:
```bash
./gradlew assembleDebug
```

To build the release APK:
```bash
./gradlew assembleRelease
```

## Project Structure

```
app/src/main/java/com/example/budget/
├── data/
│   ├── db/           # Room database entities, DAOs, converters
│   ├── BudgetRepository.kt  # Data access layer
│   └── AppContainer.kt      # Dependency injection
├── ui/
│   ├── home/         # Home screen UI and ViewModel
│   ├── budget/       # Budget management UI
│   ├── expense/      # Expense tracking UI
│   ├── summary/      # Summary/analytics UI
│   ├── settings/     # Settings and data management UI
│   └── theme/        # App theming and colors
├── BudgetApp.kt      # Application class
└── MainActivity.kt   # Main activity
```

## Key Features in Detail

### Data Export/Import
- **Export**: Creates a pretty-printed JSON file containing all categories, expenses, and budgets
- **Import**: Completely replaces existing data with imported data
- **Format**: Human-readable JSON with proper date formatting
- **Validation**: Comprehensive error handling for corrupted files

### Smart Budget Management
- **Templates**: Set up budget templates that can be reused
- **Prompts**: App prompts to create budgets when logging expenses without budgets
- **Validation**: Input validation prevents invalid budget amounts

### Advanced Expense Tracking
- **History**: View all expenses with category names, dates, and amounts
- **Validation**: Real-time input validation with visual feedback
- **Flexibility**: Edit dates, amounts, and descriptions easily

## Contributing

This project follows modern Android development best practices:
- MVVM architecture with clear separation of concerns
- Reactive programming with StateFlow
- Type-safe database operations with Room
- Comprehensive input validation
- Material 3 design guidelines

## License

[Add your license here]
