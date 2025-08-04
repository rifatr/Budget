# 💰 Budget Tracker

A beautiful, comprehensive Android expense tracking app built with Jetpack Compose and modern Android development practices. Track your monthly budgets, log expenses, and monitor your financial health with an intuitive and polished interface.

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg)
![Material 3](https://img.shields.io/badge/Design-Material%203-purple.svg)

</div>

## ✨ Features

### 💳 Multi-Currency Support
- **6 Currencies**: Taka (৳), Dollar ($), Rupee (₹), Euro (€), Pound (£), Yen (¥)
- **First Launch Setup**: Welcome dialog with currency selection (defaults to Taka)
- **Settings Integration**: Change currency anytime from settings
- **Dynamic Display**: All amounts throughout app show selected currency symbol

### 📊 Expense Management
- **Smart Input**: Tap-to-dismiss keyboard with proper field navigation
- **Date Selection**: Interactive date picker with calendar icon
- **Category Management**: Create custom categories with duplicate prevention
- **Input Validation**: 6 digits before decimal, 2 after with real-time feedback
- **Expense History**: View, search, and delete past expenses with currency symbols

### 💰 Budget Tracking
- **Monthly Budgets**: Set overall and category-specific budget limits
- **Visual Progress**: See budget vs spending with clear totals
- **Smart Validation**: Prevent duplicate categories and invalid amounts
- **Auto-Save**: Press Done to automatically save budget changes

### 📈 Summary & Analytics
- **Monthly Overview**: Budget vs actual spending comparison
- **Visual Cards**: Beautiful card-based layout with totals and dividers
- **Loading States**: Smooth loading indicators for better UX
- **Empty States**: Helpful messages when no data is available

### 🛠️ Data Management
- **Export/Import**: JSON-based backup and restore functionality
- **Progress Feedback**: Real-time status updates during operations
- **Error Handling**: Comprehensive error handling with user-friendly messages
- **Local Storage**: All data stored securely using Room database

### 🎨 Modern UI/UX
- **Bottom Navigation**: Expense, Budget, Summary, More tabs with visual feedback
- **Gradient Theme**: Eye-soothing soft blue gradient design
- **Material 3**: Latest Material Design components and principles
- **Keyboard Handling**: Smart keyboard dismissal and field navigation
- **Perfect Alignment**: Properly centered text and UI elements

### 📱 Home Screen Widget
- **Quick Expense Entry**: 1x1 home screen widget for instant expense logging
- **Beautiful Design**: Compact blue circular button with plus icon
- **Smart UI**: Dedicated expense entry screen with gradient background
- **Category Selection**: Dynamic category dropdown with most-used categories first
- **Input Validation**: Real-time amount validation (6 digits before, 2 after decimal)
- **Currency Support**: Uses your selected currency symbol in confirmations
- **Seamless Experience**: Returns to home screen after adding expense

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room with SQLite
- **Navigation**: Jetpack Compose Navigation with Bottom Tabs
- **State Management**: StateFlow and Compose State
- **Data Serialization**: Gson for JSON export/import
- **Persistence**: SharedPreferences for user settings
- **Build System**: Gradle with Kotlin DSL

### Key Components
- **Currency System**: Enum-based with reactive updates
- **Focus Management**: Smart keyboard and focus handling
- **Input Validation**: Real-time validation with visual feedback
- **Data Layer**: Repository pattern with Room database
- **UI Layer**: Composable screens with ViewModels

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 24 or higher (Android 7.0+)
- Kotlin 1.9.22

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/budget-tracker.git
   cd budget-tracker
   ```

2. Open the project in Android Studio

3. Sync the project and let Gradle download dependencies

4. Run the app on an emulator or physical device

### Building APK
For debug build:
```bash
./gradlew assembleDebug
```

For release build (requires keystore setup):
```bash
./gradlew assembleRelease
```

## 📁 Project Structure

```
app/src/main/java/com/example/budget/
├── data/
│   ├── db/                     # Room database (entities, DAOs, converters)
│   ├── Currency.kt            # Currency enum and definitions
│   ├── CurrencyPreferences.kt # Currency preference management
│   ├── BudgetRepository.kt    # Data access layer
│   └── AppContainer.kt        # Dependency injection container
├── ui/
│   ├── expense/               # Expense tracking UI and ViewModel
│   ├── budget/                # Budget management UI and ViewModel
│   ├── summary/               # Summary/analytics UI and ViewModel
│   ├── more/                  # More screen with navigation
│   ├── info/                  # App information and credits
│   ├── settings/              # Data management (export/import)
│   ├── setup/                 # Currency selection dialog
│   ├── theme/                 # App theming and gradient colors
│   ├── Navigation.kt          # Route definitions
│   ├── BudgetAppNavigation.kt # Main navigation with bottom tabs
│   └── AppViewModelProvider.kt # ViewModel factory
├── widget/
│   ├── ExpenseWidgetProvider.kt      # Widget provider and lifecycle
│   └── ExpenseWidgetConfigActivity.kt # Widget quick expense entry UI
├── BudgetApp.kt               # Application class with DI setup
└── MainActivity.kt            # Main activity with currency setup
```

## 🎯 Key Features in Detail

### Currency Management
- **Reactive Updates**: Currency changes instantly update throughout the app
- **Persistent Storage**: Selected currency saved and restored on app restart
- **First Launch**: Guided setup with currency selection
- **Settings Access**: Easy currency switching from More > App Management

### Smart Input Handling
- **Keyboard Management**: Tap anywhere to dismiss keyboard
- **Field Navigation**: Next/Done buttons for smooth field transitions
- **Auto-Save**: Done button automatically saves and dismisses keyboard
- **Focus Management**: Visual feedback for active input fields

### Data Integrity
- **Input Validation**: Real-time validation with error messages
- **Duplicate Prevention**: Cannot create categories with same names
- **Amount Limits**: Prevent overflow with 6+2 digit limits
- **Error Recovery**: Graceful handling of invalid states

### Export/Import System
- **JSON Format**: Human-readable backup files
- **Complete Data**: Exports all categories, expenses, and budgets
- **Status Feedback**: Real-time progress and success/error messages
- **File Management**: Uses Android's document picker for file operations

## 🎨 Design Principles

### Visual Design
- **Gradient Theme**: Soft blue gradient background for eye comfort
- **Material 3**: Latest design system with proper elevation and colors
- **Consistent Spacing**: 8dp grid system for perfect alignment
- **Typography**: Clear hierarchy with proper font weights

### User Experience
- **Intuitive Navigation**: Bottom tabs for easy access to main features
- **Quick Actions**: History button in expense screen for easy access
- **Visual Feedback**: Loading states, success messages, and error handling
- **Accessibility**: Proper content descriptions and keyboard navigation

## 🛡️ Quality Assurance

### Code Quality
- **MVVM Architecture**: Clear separation of concerns
- **Type Safety**: Kotlin's type system prevents runtime errors
- **State Management**: Reactive programming with StateFlow
- **Error Handling**: Comprehensive try-catch blocks and user feedback

### Testing Considerations
- **Input Validation**: Extensive validation prevents crashes
- **Database Operations**: Proper Room setup with migrations
- **UI Testing**: Compose testing-friendly structure
- **Performance**: Efficient state updates and database queries

## 📝 Usage Guide

### First Time Setup
1. Launch the app
2. Select your preferred currency (defaults to Taka)
3. Start by creating expense categories in Budget tab
4. Set your monthly budget amounts
5. Begin logging expenses in Expense tab

### Daily Usage
1. **Add Expense**: Use Expense tab with date picker and category selection
2. **Quick Entry**: Use home screen widget for instant expense logging
3. **Check Progress**: View Summary tab for budget vs spending overview
4. **Adjust Budgets**: Modify budgets in Budget tab as needed
5. **Data Management**: Export backups or change currency in More tab

## 🔄 Data Backup

### Export Data
1. Go to More > Data
2. Tap "Export Data"
3. Choose save location
4. JSON file created with all your data

### Import Data
1. Go to More > Data  
2. Tap "Import Data"
3. Select your backup JSON file
4. Data automatically restored

## 🎯 Widget Usage

### Adding Widget to Home Screen
1. Long press on your home screen
2. Tap "Widgets" 
3. Find "Budget Tracker" in the list
4. Drag the "Expense Widget" to your home screen
5. Widget appears as a compact blue circular button

### Using the Widget
1. Tap the widget button on your home screen
2. Beautiful expense entry screen opens with gradient background
3. Enter expense amount with automatic validation
4. Select category from dropdown (most-used categories appear first)
5. Tap "Add Expense" to save
6. Success message shows with your currency symbol
7. Automatically returns to home screen

## 👨‍💻 Developer Information

**Developer**: Lutfar Rahman  
**Contact**: lutfarrahman@example.com  
**Version**: 1.0.0  
**Platform**: Android 7.0+ (API 24+)

### Tech Stack Details
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Java Compatibility**: Java 8
- **Gradle**: 8.7

## 🔮 Future Enhancements

- **Data Visualization**: Charts and graphs for spending trends
- **Categories**: Pre-built category icons and smart suggestions
- **Recurring Expenses**: Support for recurring transactions
- **Cloud Sync**: Backup to cloud storage
- **Dark Mode**: Enhanced dark theme support
- **Advanced Widgets**: Configurable widget sizes and more widget types

## 🤝 Contributing

This project follows modern Android development best practices. Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Follow existing code style and architecture
4. Add proper validation and error handling
5. Test thoroughly before submitting PR

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with ❤️ using Jetpack Compose and Material 3**

</div>
