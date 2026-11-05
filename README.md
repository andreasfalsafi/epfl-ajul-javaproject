# Azul — JavaFX Interactive Board Game & AI Simulator

An interactive, animated digital adaptation of the board game **Azul**, developed in **Java 25** and JavaFX as part of coursework at **EPFL**. The project features custom 2D rendering, smooth animations, a multithreaded architecture to keep the UI responsive, and a Monte Carlo-based AI opponent.

---

## 📌 Project Overview
* **Institution:** École Polytechnique Fédérale de Lausanne (EPFL)
* **Authors:** Andreas Georgios Falsafi & Morgan Jean-Phillipe Hauswirth
* **Team Size:** 2 developers
* **Tech Stack:** Java 25, JavaFX 26, JUnit Jupiter

---

## ✨ Key Features

### 🎮 Interactive UI & Graphical Engine (JavaFX)
* **Full Board Game Simulation:** Supports **2 to 4 players** with automated score tracking, endgame triggers, and turn management.
* **Factory Displays & Center Pool:** Rectangular tile-dispensing pods and center display areas from which players draft colored tiles during the turn phase.
* **Dynamic Animations:** Smooth graphical transitions using JavaFX timelines to animate colored tiles sliding from the display pods and center pool onto player pattern lines and wall grids during the Tiling Phase.

### 🧵 Multithreaded Architecture
* **UI Thread vs. Background AI Thread:** Utilizes a multi-threaded design separating the **JavaFX Application Thread** (handling rendering, user input, and animations) from a dedicated **Background Worker Thread** (running AI calculations). This prevents UI freezes while the AI evaluates complex game trees.

### 🤖 Configurable Monte Carlo AI Opponent
* **Monte Carlo Decision Tree:** Evaluates valid move branches from the current gamestate, using semi-random rollout simulations to estimate win/loss outcomes and terminal scores.
* **Adjustable Search Budget:** The number of Monte Carlo simulations the AI runs per move is configurable (`MCTS_NUM_SIM` in `Main.java`, 100 000 by default). Fewer simulations run faster (making the AI less intelligent), while more simulations increase computation time for smarter, strategic play.

### ⚙️ Customizable Game Setup & Reproducibility
* **Run Configurations:** Fully configurable via run parameters and edit configurations.
* **Deterministic Seeding:** Supports pseudo-random seed values to control bag shuffling and factory setup for reproducible game states, debugging, and testing.

---

## 🧪 Testing Approach & Code Quality

* **Unit Testing (JUnit Jupiter):** Comprehensive unit tests covering game-state transitions, tile-bag generation, wall placement rules, floor-line penalties, and scoring algorithms (including adjacent tile bonus calculations).
* **AI Decision Validation:** Unit tests targeting AI tree nodes to verify move-ranking logic and prevent illegal move selections.

---

## 🗂️ Project Structure

```
src/ch/epfl/ajul/
├── (root)            Core model: Game, players, tile kinds/sources/destinations, scoring
├── gamestate/        Mutable & immutable game states, moves
│   └── packed/       Bit-packed representations (tile sets, walls, pattern lines, floors, moves)
├── intarray/         Lightweight read-only / mutable / immutable int arrays
├── mcts/             Monte Carlo Tree Search AI (MctsNode, MctsPlayer, heuristic move selector)
└── gui/              JavaFX interface, tile animations, entry point (Main)
resources/            Stylesheet (ajul.css)
test/                 JUnit test suites
```

---

## 🛠️ Getting Started

### Prerequisites
* **Java Development Kit (JDK):** 24 or higher (25 LTS recommended)
* **JavaFX SDK:** 26 — download it from [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx/) (JavaFX is no longer bundled with the JDK)
* **JUnit Jupiter:** 6.x — only needed to run the tests
* **IDE (recommended):** IntelliJ IDEA

> The project has no Maven/Gradle build file; dependencies are added manually as IDE libraries (see below).

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone https://github.com/andreasfalsafi/epfl-ajul-javaproject.git
   cd epfl-ajul-javaproject
   ```

2. **Open the project in IntelliJ IDEA** and set the Project SDK to JDK 24+ (*File → Project Structure → Project*).

3. **Add the libraries** (*File → Project Structure → Libraries → +*):
   * the `lib/` folder of the JavaFX SDK
   * JUnit Jupiter (*From Maven…* → `org.junit.jupiter:junit-jupiter:6.0.2`)

4. **Mark the folders** (if not detected automatically): `src` as *Sources*, `test` as *Test Sources*, `resources` as *Resources*.

5. **Run `ch.epfl.ajul.gui.Main`** with program arguments describing the players (see *Usage*).

#### Command line (alternative)

```bash
FX=/path/to/javafx-sdk-26/lib
javac --module-path "$FX" -d out $(find src -name "*.java")
cp resources/* out/
java --module-path "$FX:out" -m Ajul/ch.epfl.ajul.gui.Main Alice _Bot
```

### Usage

The program arguments are the **names of the players** (2 to 4), in turn order:

* A plain name is a **human** player.
* A name prefixed with `_` is an **AI** player (the `_` is not displayed).
* An optional `--seed=<value>` fixes the random seed so the same game can be replayed.

| Example arguments | Game |
|---|---|
| `Alice Bob` | Two humans |
| `Alice _Bot` | Human vs. AI |
| `Alice _Bot1 _Bot2 _Bot3` | One human vs. three AIs |
| `_A _B --seed=42` | Two AIs playing a reproducible game |

**How to play:** drag tiles from a factory (or the center) onto one of your pattern lines or your floor line. Valid destinations are highlighted while dragging.

### Running the Tests

In IntelliJ, right-click the `test` folder → *Run 'All Tests'*.

---

## ⚖️ Disclaimer

This is a non-commercial student project made for educational purposes. *Azul* is a trademark of its respective owners (designed by Michael Kiesling, published by Plan B Games / Next Move Games); this project is not affiliated with or endorsed by them.
