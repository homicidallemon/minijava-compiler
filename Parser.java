/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mjcompiler;

import java.text.StringCharacterIterator;

/**
 *
 * @author Victoria Moraes
 */
public class Parser
{
    private Scanner scan;
    private SymbolTable globalST;
    private SymbolTable currentST;
    private Token lToken;
    public String mensagem = new String();
    private int i;

    public Parser(String inputFile)
    {
        //Instancia a tabela de símbolos global e a inicializa
        globalST = new SymbolTable<STEntry>();
        initSymbolTable();

        //Faz o ponteiro para a tabela do escopo atual apontar para a tabela global
        currentST = globalST;

        //Instancia o analisador léxico
        scan = new Scanner(globalST, inputFile);
    }

    /*
     * Método que inicia o processo de análise sintática do compilador
     */
    public void execute()
    {
        advance();

        try
        {
            program();
        }
        catch(CompilerException e)
        {
            mensagem += e.msg;
        }
    }

    private void advance()
    {
        lToken = scan.nextToken();
        System.out.println(lToken.name + "(" + lToken.lineNumber + ")\n");
    }

    private void match(EnumToken cTokenName) throws CompilerException
    {
        if (lToken.name == cTokenName)
            advance();
        else
        {   //Erro
            throw new CompilerException("(" + lToken.lineNumber + ") Token inesperado: " + lToken.name + "//" + cTokenName);
        }
    }


    /*
     * Método para o símbolo inicial da gramática
     */
    private void program() throws CompilerException
    {
        checkClass();
        mainClass();
        while (lToken.name == EnumToken.CLASS)
            classDeclaration();
        match(EnumToken.EOF);
        mensagem +=("Compilação encerrada com sucesso\n");

    }
    
    private void checkClass() throws CompilerException
    {
        StringCharacterIterator temp = null;
        temp = (StringCharacterIterator) scan.inputIt.clone();
        Token temptk = lToken;
        while(lToken.name == EnumToken.CLASS)
        {
            match(EnumToken.CLASS);
            if(lToken.name == EnumToken.ID)
            {
                boolean inserted = globalST.add(new STEntry(lToken,lToken.value));
                if(!inserted)
                    mensagem +=("(" + lToken.lineNumber + ") Classe " + lToken.value + " já definida\n");
                advance();
            }
            else
                throw new CompilerException("(" + lToken.lineNumber +") Declaração de classe inválida\n");
            int i = 0;
            while(lToken.name != EnumToken.LBRACE)
                advance();
            advance();
            while(true)
            {
                if(lToken.name == EnumToken.LBRACE)
                    i++;
                if(lToken.name == EnumToken.RBRACE)
                    if(i > 0)
                        i--;
                    else
                        break;
                advance();
            }
            match(EnumToken.RBRACE);
        }
        scan.inputIt = temp;
        lToken = temptk;
        scan.lineNumber = 1;
    }

    private void mainClass() throws CompilerException
    {
        match(EnumToken.CLASS);
        match(EnumToken.ID);
        match(EnumToken.LBRACE);
        match(EnumToken.PUBLIC);
        match(EnumToken.STATIC);
        match(EnumToken.VOID);
        match(EnumToken.MAIN);
        match(EnumToken.LPARENTHESE);
        match(EnumToken.STRING);
        match(EnumToken.LBRACKET);
        match(EnumToken.RBRACKET);
        match(EnumToken.ID);
        match(EnumToken.RPARENTHESE);
        match(EnumToken.LBRACE);
        statement();
        match(EnumToken.RBRACE);
        match(EnumToken.RBRACE);
    }

    private void classDeclaration() throws CompilerException
    {
        currentST = new SymbolTable<STEntry>(globalST);
        match(EnumToken.CLASS);
        match(EnumToken.ID);
        if(lToken.name == EnumToken.EXTENDS)
        {
            advance();
            if(lToken.name == EnumToken.ID)
        	{
                    STEntry t = new STEntry (lToken, lToken.value);
                    if(!globalST.check(t))
                        mensagem += ("Classe " + lToken.value + " não definida\n");
                    advance();
        	}
        }
        match(EnumToken.LBRACE);
        while(lToken.name == EnumToken.ID || lToken.name == EnumToken.BOOLEAN
            || lToken.name == EnumToken.INT)
            varDeclaration();
        while(lToken.name == EnumToken.PUBLIC)
            methodDeclaration();
        match(EnumToken.RBRACE);
    }

    private void varDeclaration() throws CompilerException
    {
        String tipo;
        tipo = type();
        if(lToken.name == EnumToken.ID)
    	{
    		boolean inserted = currentST.add(new STEntry (lToken, lToken.value,tipo));

    		if(!inserted)
    			mensagem += ("Variável " + lToken.value + " já definida\n");
    		advance();
    	}
        match(EnumToken.SEMICOLON);
    }

    private void methodDeclaration() throws CompilerException
    {
        match(EnumToken.PUBLIC);
        type();
        if(lToken.name == EnumToken.ID)
    	{
    		boolean inserted = currentST.add(new STEntry (lToken, lToken.value));

    		if(!inserted)
    			mensagem += ("Método " + lToken.value + " já definido\n");
    		advance();
    	}
        
        currentST = new SymbolTable<STEntry>(currentST);
        
        match(EnumToken.LPARENTHESE);
        
        if(lToken.name == EnumToken.ID || lToken.name == EnumToken.BOOLEAN
            || lToken.name == EnumToken.INT)
        {
            type();
            if(lToken.name == EnumToken.ID)
            {
                boolean inserted = currentST.add(new STEntry(lToken, lToken.value));

                if (!inserted) {
                    mensagem += ("Variável " + lToken.value + " já em uso no escopo\n");
                }
                advance();
            }
            while(lToken.name == EnumToken.COMMA)
            {
                match(EnumToken.COMMA);
                String tipo;
                tipo = type();
                if(lToken.name == EnumToken.ID)
            	{
            		boolean inserted = currentST.add(new STEntry (lToken, lToken.value,tipo));

            		if(!inserted)
            			 mensagem += ("(" + lToken.lineNumber +  ") " + "Variável " + lToken.value + " já em uso no escopo\n");
            		advance();
            	}
            }
        }
        match(EnumToken.RPARENTHESE);
        match(EnumToken.LBRACE);
        
        while (lToken.name == EnumToken.ID || lToken.name == EnumToken.BOOLEAN
            || lToken.name == EnumToken.INT)
        {
            StringCharacterIterator temp = null;
            temp = (StringCharacterIterator) scan.inputIt.clone();
            Token temptk = lToken;
            int ln = scan.lineNumber;
            try{
                varDeclaration();
            }
            catch(CompilerException e)
            {
                lToken = temptk;
                scan.inputIt = temp;
                scan.lineNumber = ln;
                //System.out.println(lToken.name);
                break;
            }
        }

        while(lToken.name == EnumToken.LBRACE || lToken.name == EnumToken.IF
            || lToken.name == EnumToken.WHILE || lToken.name == EnumToken.ID
            || lToken.name == EnumToken.SOPRINTLN)
            statement();
        
        match(EnumToken.RETURN);
        expression();
        match(EnumToken.SEMICOLON);
        match(EnumToken.RBRACE);
        currentST = currentST.parent;
    }

    private String type() throws CompilerException
    {
        String tipo;
        if(lToken.name == EnumToken.ID)
    	{
            STEntry t = new STEntry(lToken,lToken.value);
            if(!globalST.check(t))
                mensagem += ("(" + lToken.lineNumber + ")" + "Variável de tipo " + lToken.value + " inválida\n");
            tipo = lToken.value;
            advance();
    	}
        else if(lToken.name == EnumToken.BOOLEAN){
            tipo = "boolean";
            match(EnumToken.BOOLEAN);
        }
        else
        {
            tipo = lToken.value;
            match(EnumToken.INT);
            if(lToken.name == EnumToken.LBRACKET)
            {
                match(EnumToken.LBRACKET);
                match(EnumToken.RBRACKET);
                tipo = "intv";
            }
        }
        return tipo;
    }

    private void statement() throws CompilerException
    {
        if(lToken.name == EnumToken.ID)
        {
            String tipo = currentST.get(lToken.value).type;
            STEntry t = new STEntry (lToken, lToken.value);
            boolean inserted = (currentST.check(t) || currentST.parent.check(t));

            if(!inserted)
    		mensagem += ("(" + lToken.lineNumber +  ") " + "Variável " + lToken.value +" não declarada\n");
            
            advance();
            if(lToken.name == EnumToken.LBRACKET)
            {
                advance();
                expression();
                match(EnumToken.RBRACKET);
                match(EnumToken.ATTRIB);
                expression();
                match(EnumToken.SEMICOLON);
            }
            else if(lToken.name == EnumToken.ATTRIB)
            {
                advance();
                int line = lToken.lineNumber;
                String var = lToken.value;
                if(expression() != tipo)
                    mensagem += ("(" + line + ") Variável " + var + " não condiz com seu tipo\n");
                match(EnumToken.SEMICOLON);
            }
        }
        else if(lToken.name == EnumToken.SOPRINTLN)
        {
            match(EnumToken.SOPRINTLN);
            match(EnumToken.LPARENTHESE);
            expression();
            match(EnumToken.RPARENTHESE); 
            match(EnumToken.SEMICOLON);
           
        }
        else if(lToken.name == EnumToken.WHILE)
        {
            match(EnumToken.WHILE);
            match(EnumToken.LPARENTHESE);
            expression();
            match(EnumToken.RPARENTHESE);
            statement();
        }
        else if(lToken.name == EnumToken.IF)
        {
            match(EnumToken.IF);
            match(EnumToken.LPARENTHESE);
            int line = lToken.lineNumber;
            if(expression() != "boolean")
                mensagem += ("(" + line + ") Expressão inválida\n");
            match(EnumToken.RPARENTHESE);
            statement();
            match(EnumToken.ELSE);
            statement();
        }
        else
        {
            match(EnumToken.LBRACE);
            while(lToken.name == EnumToken.IF || lToken.name == EnumToken.WHILE
                || lToken.name == EnumToken.LBRACE || lToken.name == EnumToken.ID
                || lToken.name == EnumToken.SOPRINTLN)
                statement();
            match(EnumToken.RBRACE);
        }
    }
    
    private String expression() throws CompilerException
    {
        String tipo = new String();
        if(lToken.name == EnumToken.INTEGER_LITERAL)
        {
            advance();
            tipo = "int";
        }
        else if(lToken.name == EnumToken.TRUE)
        {
            advance();
            tipo = "boolean";
        }
        else if(lToken.name == EnumToken.FALSE)
        {
            advance();
            tipo = "boolean";
        }
        else if (lToken.name == EnumToken.ID) {

            STEntry t = new STEntry(lToken, lToken.value);
            boolean inserted = (globalST.check(t) || currentST.check(t));
            if (!inserted) {
                mensagem += ("(" + lToken.lineNumber + ") Variável " + lToken.value + " não declarada\n");
            }
            t = null;
            t = currentST.get(lToken.value);
            if (t == null) {
                t = globalST.get(lToken.value);
            }
            tipo = t.type;
            advance();
        }
        else if(lToken.name == EnumToken.THIS)
        {
            tipo = "reserved";
            advance();
        }
        else if(lToken.name == EnumToken.NEW)
        {
            advance();
            if (lToken.name == EnumToken.INT) {
                advance();
                match(EnumToken.LBRACKET);
                int line = lToken.lineNumber;
                String temp = expression();
                if (temp != "int") {
                    mensagem += ("(" + line + ") Declaração inválida!\n");
                }
                match(EnumToken.RBRACKET);
                tipo = "intv";
            }
            else if (lToken.name == EnumToken.ID) {
                STEntry t = new STEntry(lToken, lToken.value);
                boolean inserted = currentST.add(t);
                if (inserted) {
                    mensagem += ("(" + lToken.lineNumber + ") Variável " + lToken.value);
                }
                tipo = lToken.value;
                advance();
                match(EnumToken.LPARENTHESE);
                match(EnumToken.RPARENTHESE);
            }
            else
                throw new CompilerException("(" + lToken.lineNumber + ") Expressão inválida\n");
        }
        else if(lToken.name == EnumToken.NOT)
        {
            advance();
            int line = lToken.lineNumber;
            if(expression() != "boolean")
                mensagem += ("(" + line + ") Expressão inválida\n");
            tipo = "boolean";
        }
        else if(lToken.name == EnumToken.LPARENTHESE)
        {
            advance();
            tipo = expression();
            match(EnumToken.RPARENTHESE);
        }
        if(lToken.name == EnumToken.PERIOD)
        {
            advance();
            if(lToken.name == EnumToken.LENGTH)
            {
                advance();
                tipo = "int";
            }
            else if(lToken.name == EnumToken.ID)
            {
                advance();
                match(EnumToken.LPARENTHESE);
                int ln = scan.lineNumber;
                StringCharacterIterator temp = null;
                temp = (StringCharacterIterator) scan.inputIt.clone();
                Token temptk = lToken;
                try{
                    expression();
                    while(true){
                        StringCharacterIterator temp2 = null;
                        int ln2 = scan.lineNumber;
                        temp2 = (StringCharacterIterator) scan.inputIt.clone();
                        Token temptk2 = lToken;
                        try
                        {
                            match(EnumToken.COMMA);
                            expression();
                        }
                        catch(CompilerException e)
                        {
                            scan.inputIt = temp2;
                            scan.lineNumber = ln2;
                            lToken = temptk2;
                            break;
                        }
                    }
                }
                catch(CompilerException e)
                {
                    scan.inputIt = temp;
                    scan.lineNumber = ln;
                    lToken = temptk;
                }
                match(EnumToken.RPARENTHESE);
            }
            else throw new CompilerException("(" + lToken.lineNumber + ") Expressão inválida\n");
        }
        else if(lToken.name == EnumToken.LBRACKET)
        {
            advance();
            expression();
            match(EnumToken.RBRACKET);
            tipo = "intv";
        }
        else if(lToken.attribute == EnumToken.ARITHOP || lToken.attribute == EnumToken.RELOP
                || lToken.attribute == EnumToken.LOGOP)
        {
            if(lToken.attribute == EnumToken.ARITHOP)
                tipo = "int";
            else
                tipo = "boolean";
            advance();
            op();
            expression();
        }
        return tipo;
    }

    /*private String expression() throws CompilerException
    {
        String tipo = new String();
        if(lToken.name == EnumToken.INTEGER_LITERAL){
            advance();
            tipo =  "int";
        }
        else if(lToken.name == EnumToken.TRUE){
            advance(); 
            tipo = "boolean";
        }
        else if(lToken.name == EnumToken.FALSE){
            advance();
            tipo = "boolean";
        }
        else if (lToken.name == EnumToken.ID) {
            STEntry t = new STEntry(lToken, lToken.value);
            boolean inserted = currentST.check(t);

            if (!inserted) {
                mensagem += ("(" + lToken.lineNumber + ") Variável " + lToken.value + " não definida\n");
            }
            t = currentST.get(lToken.value);
            tipo = t.type;
            advance();
        }
            
        else if(lToken.name == EnumToken.THIS){
            advance();
        }
        else if(lToken.name == EnumToken.NEW)
        {
            advance();
            if(lToken.name == EnumToken.INT)
            {
                advance();
                match(EnumToken.LBRACKET);
                if(expression() != "int")
                    mensagem += ("(" + lToken.lineNumber + ") Expressão inválida\n");
                match(EnumToken.RBRACKET);
                return "int";
            }
            else if(lToken.name == EnumToken.ID)
            {
                boolean inserted = globalST.check(new STEntry (lToken, lToken.value));
            	if(!inserted)
            		mensagem += ("(" + lToken.lineNumber + ") Tipo " + lToken.value + " não definido\n");
                tipo = lToken.value;
                advance();
                match(EnumToken.LPARENTHESE);
                match(EnumToken.RPARENTHESE);
            }
        }
        else if(lToken.name == EnumToken.NOT){
            advance();
            expression();
            tipo = "bollean";
        }
        else if(lToken.name == EnumToken.LPARENTHESE)
        {
            advance();
            tipo = expression();
            match(EnumToken.RPARENTHESE);
        }
        if(tipo == "")
            tipo = expressionAux();
        else
            expressionAux();
        return tipo;
    }

    private String expressionAux() throws CompilerException
    {
        String tipo = new String();
        if(lToken.name == EnumToken.LBRACKET)
        {
            match(EnumToken.LBRACKET);
            int line = lToken.lineNumber;
            if(expression() != "int")
                mensagem += ("(" + line + ") Expressão inválida\n");
            match(EnumToken.RBRACKET);
            expressionAux();
            tipo = "intv";
        }
        else if(lToken.name == EnumToken.PERIOD)
        {
            match(EnumToken.PERIOD);
            if(lToken.name == EnumToken.LENGTH)
            {
                advance();
                expressionAux();
            }
            else if (lToken.name == EnumToken.ID) {
                STEntry t = new STEntry(lToken, lToken.value);
                boolean inserted = currentST.check(t);

                if (!inserted) {
                    mensagem += ("(" + lToken.lineNumber + ") Variável " + lToken.value + " já existente\n");
                }

                
                advance();
                match(EnumToken.LPARENTHESE);
                if(lToken.name == EnumToken.INTEGER_LITERAL ||
                    lToken.name == EnumToken.TRUE || lToken.name == EnumToken.ID
                 || lToken.name == EnumToken.FALSE || lToken.name == EnumToken.NEW
                 || lToken.name == EnumToken.THIS || lToken.name == EnumToken.NOT
                 || lToken.name == EnumToken.LPARENTHESE)
                {
                    expression();
                    while(lToken.name == EnumToken.COMMA)
                    {
                        match(EnumToken.COMMA);
                        expression();
                    }
                }
                match(EnumToken.RPARENTHESE);
                expressionAux();
            }
        }
        else if(lToken.name == EnumToken.LT || lToken.name == EnumToken.GT ||
            lToken.name == EnumToken.EQ || lToken.name == EnumToken.NE ||
            lToken.name == EnumToken.PLUS || lToken.name == EnumToken.MINUS ||
            lToken.name == EnumToken.MULT || lToken.name == EnumToken.DIV ||
            lToken.name == EnumToken.AND)
        {
            op();
            int line = lToken.lineNumber;
            if(tipo != "int" || expression() != "int"){
                mensagem += ("(" + line + ") Expressão inválida\n");
                tipo = "0";
            }
            else
                tipo = "int";
            expressionAux();
        }
        else
            ;
        return tipo;
    }*/

    private void op() throws CompilerException
    {
        if(lToken.name == EnumToken.LT)
            match(EnumToken.LT);
        else if(lToken.name == EnumToken.GT)
            match(EnumToken.GT);
        else if(lToken.name == EnumToken.EQ)
            match(EnumToken.EQ);
        else if(lToken.name == EnumToken.NE)
            match(EnumToken.NE);
        else if(lToken.name == EnumToken.PLUS)
            match(EnumToken.PLUS);
        else if(lToken.name == EnumToken.MINUS)
            match(EnumToken.MINUS);
        else if(lToken.name == EnumToken.MULT)
            match(EnumToken.MULT);
        else if(lToken.name == EnumToken.DIV)
            match(EnumToken.DIV);
        else if(lToken.name == EnumToken.AND)
            match(EnumToken.AND);
    }

    private void initSymbolTable()
    {
        Token t = new Token(EnumToken.BOOLEAN);
        globalST.add(new STEntry(t, "boolean", true));
	    t = new Token(EnumToken.CLASS);
        globalST.add(new STEntry(t, "class", true));
	    t = new Token(EnumToken.ELSE);
        globalST.add(new STEntry(t, "else", true));
	    t = new Token(EnumToken.EXTENDS);
        globalST.add(new STEntry(t, "extends", true));
	    t = new Token(EnumToken.FALSE);
        globalST.add(new STEntry(t, "false", true));
	    t = new Token(EnumToken.IF);
        globalST.add(new STEntry(t, "if", true));
	    t = new Token(EnumToken.INT);
        globalST.add(new STEntry(t, "int", true));
	    t = new Token(EnumToken.LENGTH);
        globalST.add(new STEntry(t, "length", true));
	    t = new Token(EnumToken.MAIN);
        globalST.add(new STEntry(t, "main", true));
	    t = new Token(EnumToken.NEW);
        globalST.add(new STEntry(t, "new", true));
	    t = new Token(EnumToken.PUBLIC);
        globalST.add(new STEntry(t, "public", true));
	    t = new Token(EnumToken.RETURN);
        globalST.add(new STEntry(t, "return", true));
	    t = new Token(EnumToken.STATIC);
        globalST.add(new STEntry(t, "static", true));
	    t = new Token(EnumToken.SOPRINTLN);
        globalST.add(new STEntry(t, "System.out.println", true));
	    t = new Token(EnumToken.STRING);
        globalST.add(new STEntry(t, "String", true));
	    t = new Token(EnumToken.THIS);
        globalST.add(new STEntry(t, "this", true));
	    t = new Token(EnumToken.TRUE);
        globalST.add(new STEntry(t, "true", true));
	    t = new Token(EnumToken.VOID);
        globalST.add(new STEntry(t, "void", true));
	    t = new Token(EnumToken.WHILE);
        globalST.add(new STEntry(t, "while", true));
   }
}
