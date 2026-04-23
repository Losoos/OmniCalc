# Equation Calculator (Kalkulačka rovnic)

Modern Material You calculator for Android that solves linear equations and systems of equations.

## Latest Version / Nejnovější verze
[![Download v1.0.0](https://img.shields.io/badge/Download-v2.0.0-green?style=for-the-badge&logo=android)](https://github.com/Losoos/-Equation-calculator-for-android/releases/download/v2.0.0/caclc-v2.0.0.apk)

[See all versions / Všechny verze](https://github.com/Losoos/-Equation-calculator-for-android/releases)

## Features / Funkce
* **Equation Solver**: Solves linear equations and systems (separated by `;`).
* **Multi-variable support**: Handles variables `a-z` (long-press `x` to show the variable bar).
* **Scientific functions**: Supports `√`, `^`, `π`, `e`.
* **Currency Converter**: Real-time currency conversion using official rates from the European Central Bank.
* **Multi-language**: Support for 10 languages including Czech, English, Slovak, German, Spanish, French, Arabic, Hindi, Chinese, and Japanese.
* **Localization**: Automatic decimal separator (comma/dot) and digit support based on locale.
* **Material You**: Beautiful pill-shaped buttons and dynamic colors.

## Technology / Technologie
* **Language**: Written entirely in **Kotlin**.
* **UI**: Material Design 3 (Material You).

## Usage / Použití
1. Enter your equation (e.g., `2x + 5 = 15`).
2. Press the `?` button to solve.
3. Long-press the `x` button to choose other variables.

---

1. Zadejte rovnici (např. `2x + 5 = 15`).
2. Stiskněte tlačítko `?` pro výpočet.
3. Soustavy rovnic oddělujte středníkem `;` (např. `x+y=10; x-y=2`).
4. Podržením tlačítka `x` zobrazíte lištu s dalšími písmeny.

## Data Source & Disclaimer / Zdroj dat a prohlášení
* **Source**: Exchange rates are provided by the **European Central Bank (ECB)**.
* **Update**: Data is updated daily (usually around 16:00 CET).
* **Disclaimer**: This application is for informational purposes only. The author is not responsible for any financial losses caused by the use of this data.
* **Official Website**: [ECB Statistical Data Warehouse](https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_fx_ref/html/index.en.html)

---

* **Zdroj**: Směnné kurzy jsou poskytovány **Evropskou centrální bankou (ECB)**.
* **Aktualizace**: Data jsou aktualizována denně (obvykle kolem 16:00 SEČ).
* **Prohlášení**: Tato aplikace slouží pouze pro informační účely. Autor nenese odpovědnost za případné finanční ztráty způsobené použitím těchto dat.
* **Oficiální stránky**: [ECB Statistical Data Warehouse](https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_fx_ref/html/index.en.html)

## Author / Autor
Created by **Losoos**

## Recent Updates / Poslední změny
* **Dashboard & Navigation**: Added a new main menu (Dashboard) for easy access to different tools.
* **Currency Converter**: Integrated real-time currency conversion using data from the European Central Bank.
* **Package Cleanup**: Reorganized the project structure to the professional `cz.losoos.calculator` namespace, removing all default "example" references.
* **Test Refactoring**: Updated unit and instrumentation tests to match the new package structure.
* **Repository Cleanup**: Removed IDE-specific files (`.idea/`) from the repository to ensure a clean codebase.
* **UI Fix**: Adjusted button text sizes to ensure "Vypočítat" fits perfectly on all screens.

## License / Licence
Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
