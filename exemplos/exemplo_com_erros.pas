// ==========================================
// Exemplo 2: Demonstração de Detecção e
// Recuperação de Erros Léxicos (Modo Pânico)
// ==========================================
PROGRAM TesteErros;
VAR
   nomeValido : STRING;
   // ERRO 1: Identificador excede o limite máximo de 15 caracteres
   esteIdentificadorTemMaisDeQuinzeCaracteres : INTEGER;
   // ERRO 2: Caracteres especiais proibidos ($ e #)
   salario$Base : REAL;
   valor#Total : REAL;
   // ERRO 3: Identificador inválido iniciando por dígitos
   99nomes : STRING;
   // ERRO 4: Número real malformado (ponto sem dígitos)
   taxa : REAL;
BEGIN
   taxa := 42.abc;
   salario$Base := 1500.50;
   
   // ERRO 5: Literal de texto não fechado antes do fim da linha
   nomeValido := 'Texto sem aspas no final;
   
   esteIdentificadorTemMaisDeQuinzeCaracteres := 10;
END.
