# Static-Program-Analizer
You can find all project requirements in the [Handbook](https://github.com/TheYoungBeast/Static-Program-Analizer/blob/master/INF2ATS%20Handbook.pdf).
## Note
The author of the Handbook and Course is prof. dr. Stanisław Jarząbek which created this course for Bialystok's Univeristy of Technology.
The are 2 authors of the this project.
Project contains over 155 commits, where almost over 100 belongs to me.
I was resposible for following modules:
- CFG
- PKB (improvements & modifications)
- QueryTree
- EvaluationEngine
- QueryPreprocessor
- QueryEvaluator
- QueryProjector
- Improving the efficiency of evaluation algorithms

Rest of the modules such that:
- AST
- PKB
- Parser
- Lexer

were done by [@Crysisek](https://github.com/Crysisek)

# 4.2 What Is an SPA and How Is It Used?
A Static Program Analyzer (SPA for short) is an interactive tool that automatically answers queries 
about programs. Of course, an SPA cannot answer all possible program queries. But the class of 
program queries that can be answered automatically is wide enough to make an SPA a useful tool. In 
your programming practice, you might have used a cross-referencing tool. An SPA is an enhanced 
version of a cross-referencing tool. In this project, you will design and implement an SPA for a simple 
source language. 
The following scenario describes how an SPA is used by programmers:
1. John, a programmer, is given a task to fix an error in a program.
2. John feeds the program into SPA for automated analysis. The SPA parses a program into the internal 
representation stored in a Program Knowledge Base (PKB).
3. Now, John can start using SPA to help him find program statements that cause the error. John 
repeatedly enters queries to the SPA. The SPA evaluates queries and displays results. John analyzes 
query results and examines related sections of the program trying to locate the source of the error. 
4. John finds program statement(s) responsible for an error. Now he is ready to modify the program
to fix the error. Before that, John can ask the SPA more queries to examine a possible unwanted 
ripple effect of changes he intends to do. 
From a programmer’s point of view, there are three use cases: source program entering and automated 
analysis, query entering and processing, and viewing query results

# 4.3 How Does an SPA Work?
In order to answer program queries, an SPA must first analyze a source program and extract relevant 
program design abstractions. Program design abstractions that are useful in answering queries typically 
include an Abstract Syntax Tree (AST), a program Control Flow Graph (CFG) and cross-reference lists 
indicating usage of program variables, procedure invocations, etc. An SPA Front-End (Figure 1) parses 
a source program, extracts program design abstractions and stores them in a Program Knowledge Base 
(PKB). 
Now, we need to provide a programmer with means to ask questions about programs. While using plain 
English would be simple for programmers, it would be very difficult for SPAs. Therefore, as a workable 
compromise, we define a semi-formal Program Query Language (PQL for short) for a programmer to 
formulate program queries. A Query Processor validates and evaluates queries. Query Result Projector
displays query results for the programmer to view.

# Diagrams

![obraz](https://github.com/TheYoungBeast/Static-Program-Analizer/assets/19922252/b4bf74d3-4730-4605-b7d4-8954f9481f63)

![obraz](https://github.com/TheYoungBeast/Static-Program-Analizer/assets/19922252/617c71de-bca0-4b47-8447-43002280b685)

![obraz](https://github.com/TheYoungBeast/Static-Program-Analizer/assets/19922252/86bd69f1-1f1c-4f8a-acfc-8afe782c5207)

![obraz](https://github.com/TheYoungBeast/Static-Program-Analizer/assets/19922252/a020421e-d3dc-4345-baca-7afedc0d0479)

![obraz](https://github.com/TheYoungBeast/Static-Program-Analizer/assets/19922252/6ca23f4f-da27-4cf3-80c2-7d3f90035e04)
