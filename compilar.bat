@echo off
echo ========================================================
echo Compilando Projeto PascaLite em Portugues (Java 21)
echo ========================================================

if not exist bin mkdir bin

javac -encoding UTF-8 -d bin src\br\ufes\compiladores\pascalite\lexico\*.java src\br\ufes\compiladores\pascalite\lexico\afd\*.java src\br\ufes\compiladores\pascalite\ide\modelos\*.java src\br\ufes\compiladores\pascalite\ide\*.java src\br\ufes\compiladores\pascalite\Principal.java

if errorlevel 1 (
    echo Erro durante a compilacao!
    pause
    exit /b 1
)

echo Gerando arquivo JAR executavel pascalite.jar...
jar cfe pascalite.jar br.ufes.compiladores.pascalite.Principal -C bin .

echo Compilacao concluida com sucesso!
echo Para rodar a IDE: executar_ide.bat ou java -jar pascalite.jar
echo Para rodar no terminal: java -jar pascalite.jar exemplos\exemplo_valido.pas
pause
