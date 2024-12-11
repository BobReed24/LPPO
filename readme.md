# Lppo Interpreter

Lppo Interpreter is a custom interpreter for the LPPO (Language of Processed Operations) programming language. This guide provides an overview of the language, its features, and how to use the interpreter. Note that window creation is currently in alpha and still requires testing. LPPO is open source.

## Table of Contents

- [Introduction](#introduction)
- [Installation](#installation)
- [Usage](#usage)
- [Language Features](#language-features)
  - [Defining Functions](#defining-functions)
  - [Running Functions](#running-functions)
  - [System Commands](#system-commands)
  - [Variable Assignment](#variable-assignment)
  - [Input Handling](#input-handling)
  - [Output Handling](#output-handling)
  - [Window Creation](#window-creation)
- [Example Script](#example-script)
- [Contributing](#contributing)
- [License](#license)

## Introduction

Lppo Interpreter is designed to interpret scripts written in the LPPO programming language. The language supports basic arithmetic operations, function definitions, input/output handling, and window creation (in alpha).

## Installation

To use Lppo Interpreter, you need to have Java installed on your system. You can download and install Java from [here](https://www.oracle.com/java/technologies/javase-downloads.html).

Clone the repository and compile the Java files:

```sh
git clone https://github.com/BobReed24/LPPO.git
cd LppoInterpreter
javac LppoInterpreter.java
```

## Usage

To run the interpreter, use the following command:

```sh
java LppoInterpreter <path_to_lppo_file>
```

Replace `<path_to_lppo_file>` with the path to your LPPO script file.

## Language Features

### Defining Functions

Functions in LPPO are defined using the `define-function` keyword. The function body is enclosed in curly braces `{}`.

```lppo
define-function (myFunction) {
    // function body
}
```

### Running Functions

To run a defined function, use the `runner.run` command followed by the function name.

```lppo
runner.run(myFunction);
```

### System Commands

- `sys.start;` - Indicates the start of the program.
- `sys.pause;` - Pauses the execution and waits for user input to continue.
- `sys.end;` - Indicates the end of the program.

### Variable Assignment

Variables can be assigned values using the `<<` operator.

```lppo
variableName << value;
```

### Input Handling

To prompt the user for input, use the `runner.input` command.

```lppo
runner.input: "Enter your name" << userName;
```

### Output Handling

To print output to the terminal, use the `runner.terminal` command.

```lppo
runner.terminal: "Hello, <<userName>>!";
```

### Window Creation

Window creation is currently in alpha and requires testing. Use the following commands to create and customize a window:

- `runner.window.size: width, height;` - Sets the window size.
- `runner.window.title: "Title";` - Sets the window title.
- `runner.window.position: x, y;` - Sets the window position.
- `runner.window.color: r, g, b;` - Sets the window background color.
- `runner.window.text.color: r, g, b;` - Sets the text color.
- `runner.window.text.size: size;` - Sets the text size.
- `runner.window.text: "Text";` - Sets the window text.

## Example Script

Here is an example LPPO script:

```lppo
sys.start;

define-function (windowPrint) {
    runner.window.size: 800, 600;
    runner.window.title: "My First Test";
    runner.window.position: 100, 100;
    runner.window.color: 255, 255, 255;
    runner.window.text.color: 0, 0, 0;
    runner.window.text.size: 1.2rem;
    runner.window.text: "Hello, World!";
}

runner.run(windowPrint);

sys.end;
```

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
