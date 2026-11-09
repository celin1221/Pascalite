PROGRAM TesteErros;
VAR
   nomeValido : STRING;
   esteIdentificadorTemMaisDeQuinzeCaracteres : INTEGER;
   salario$Base : REAL;
   valor#Total : REAL;
   99nomes : STRING;
   taxa : REAL;
BEGIN
   taxa := 42.abc;
   salario$Base := 1500.50;
   
   // Instrucao apenas com ID (gera Warning sintatico)
   nomeValido;
   
   // Erro sintatico: falta o THEN
   IF taxa > 10
      taxa := 0;
   
   nomeValido := 'Texto sem aspas no final;
   
   esteIdentificadorTemMaisDeQuinzeCaracteres := 10;
END.
