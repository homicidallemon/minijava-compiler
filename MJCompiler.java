/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mjcompiler;

/**
 *
 * @author Victoria Moraes e Lucas Reis
 */
public class MJCompiler
{
    public static void main(String[] args)
    {
        globalST = new SymbolTable<STEntry>();
        initSymbolTable();

        Scanner scanner = new Scanner(globalST, "teste1.mj");

        Token tok;

        //double var = 2.e+10;

        do
        {
            tok = scanner.nextToken();
            System.out.print(tok.name + " ");
        } while (tok.name != EnumToken.EOF);

    }

}
