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
        System.out.println(lToken.name + "(" + lToken.lineNumber + ")" + "\n" );
    }

    private void match(EnumToken cTokenName) throws CompilerException
    {
        if (lToken.name == cTokenName)
            advance();
        else
        {   //Erro
            throw new CompilerException("Token inesperado: " + lToken.name + "//" + cTokenName);
        }
    }


    /*
     * Método para o símbolo inicial da gramática
     */
    private void program() throws CompilerException
    {
        mainClass();
        while (lToken.name == EnumToken.CLASS)
            classDeclaration();

        match(EnumToken.EOF);
        mensagem +=("Compilação encerrada com sucesso\n");

    }

    private void mainClass() throws CompilerException
    {
        match(EnumToken.CLASS);
        if(lToken.name == EnumToken.ID)
    	{
    		boolean inserted = globalST.add(new STEntry (lToken, lToken.value));

    		if(!inserted)
    			mensagem += ("Classe " + lToken.value + " já definida\n");
    		advance();
    	}
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
        match(EnumToken.CLASS);
        if(lToken.name == EnumToken.ID)
    	{
    		boolean inserted = globalST.add(new STEntry (lToken, lToken.value));

    		if(!inserted)
    			mensagem += ("Classe " + lToken.value + " já definida\n");
    		advance();
    	}
        if(lToken.name == EnumToken.EXTENDS)
        {
            match(EnumToken.EXTENDS);
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
        type();
        if(lToken.name == EnumToken.ID)
    	{   /////////////////////////////////////////////////////////////////////////
    		boolean inserted = currentST.add(new STEntry (lToken, lToken.value));

    		if(!inserted)
    			mensagem += ("Variável " + lToken.value + " já definida\n");*/
    		advance();
    	}
        match(EnumToken.SEMICOLON);
    }

    private void methodDeclaration() throws CompilerException
    {/////////////////////////////////////////////////////////////////////////
        match(EnumToken.PUBLIC);
        type();
        if(lToken.name == EnumToken.ID)
    	{
    		boolean inserted = currentST.add(new STEntry (lToken, lToken.value));

    		if(!inserted)
    			mensagem += ("Método " + lToken.value + " já definido\n");
    		advance();
    	}
        
        currentST = new SymbolTable<STEntry>(globalST);////////////////////////////////////////////
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
                type();
                if(lToken.name == EnumToken.ID)
            	{
            		boolean inserted = currentST.add(new STEntry (lToken, lToken.value));

            		if(!inserted)
            			 mensagem += ("Variável " + lToken.value + " já em uso no escopo\n");
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
            try{
                varDeclaration();
            }
            catch(CompilerException e)
            {
                lToken = temptk;
                scan.inputIt = temp;
                //System.out.println(lToken.name);
                break;
            }
        }

        while(lToken.name == EnumToken.LBRACE || lToken.name == EnumToken.IF
            || lToken.name == EnumToken.WHILE || lToken.name == EnumToken.ID
            || lToken.name == EnumToken.SOPRINTLN)
            statement(currentST);
        
        match(EnumToken.RETURN);
        expression(currentST);
        match(EnumToken.SEMICOLON);
        match(EnumToken.RBRACE);
        currentST = currentST.parent;/////////////////////////////////////////
        //desfazer arvore do escopo
    }

    private void type() throws CompilerException
    {
        if(lToken.name == EnumToken.ID)
    	{
            Token t = new Token(lToken.value);
            globalST.add(new STEntry(t, lToken.value, true));
    		advance();
    	}
        else if(lToken.name == EnumToken.BOOLEAN)
            match(EnumToken.BOOLEAN);
        else
        {
            match(EnumToken.INT);
            if(lToken.name == EnumToken.LBRACKET)
            {
                match(EnumToken.LBRACKET);
                match(EnumToken.RBRACKET);
            }
        }
    }

    private void statement() throws CompilerException
    {
        if(lToken.name == EnumToken.ID)
        {
            t = new STEntry (lToken, lToken.value);
            boolean inserted = currentST.check(t);

            if(!inserted)
    		System.out.printf("Variável %s não definida\n", lToken.value);
            
            advance();
            if(lToken.name == EnumToken.LBRACKET)
            {
                match(EnumToken.LBRACKET);
                expression();
                match(EnumToken.RBRACKET);
                match(EnumToken.ATTRIB);
                expression();
                match(EnumToken.SEMICOLON);
            }
            else if(lToken.name == EnumToken.ATTRIB)
            {
                match(EnumToken.ATTRIB);
                
                expression();
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
            expression();
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

    private void expression() throws CompilerException
    {
        if(lToken.name == EnumToken.INTEGER_LITERAL)
            advance();
        else if(lToken.name == EnumToken.TRUE)
            advance(); 
        else if(lToken.name == EnumToken.FALSE)
            advance();
        else if (lToken.name == EnumToken.ID) {
            t = new STEntry(lToken, lToken.value);
            boolean inserted = currentST.check(t);

            if (!inserted) {
                System.out.printf("Variável %s não definida\n", lToken.value);
            }
            advance();
        }
            
        else if(lToken.name == EnumToken.THIS)
            advance();
        else if(lToken.name == EnumToken.NEW)
        {
            advance();
            if(lToken.name == EnumToken.INT)
            {
                advance();
                match(EnumToken.LBRACKET);
                expression();
                match(EnumToken.RBRACKET);
            }
            else if(lToken.name == EnumToken.ID)
            {
                boolean inserted = globalST.add(new STEntry (lToken, lToken.value));

            	if(!inserted)
            		System.out.printf("Classe %s já definida\n", lToken.value);
                
                advance();
                match(EnumToken.LPARENTHESE);
                match(EnumToken.RPARENTHESE);
            }
        }
        else if(lToken.name == EnumToken.NOT){
            advance();
            expression();
        }
        else if(lToken.name == EnumToken.LPARENTHESE)
        {
            advance();
            expression();
            match(EnumToken.RPARENTHESE);
        }
        expressionAux();
    }

    private void expressionAux() throws CompilerException
    {
        if(lToken.name == EnumToken.LBRACKET)
        {
            match(EnumToken.LBRACKET);
            expression();
            match(EnumToken.RBRACKET);
            expressionAux();
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
                t = new STEntry(lToken, lToken.value);
                boolean inserted = currentST.check(t);

                if (!inserted) {
                    System.out.printf("Variável %s não definida\n", lToken.value);
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
            expression();
            expressionAux();
        }
        else
        ;
    }

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
