/**
	Author: James Roberson III
	Course: Compiler Design
	Date: February 28, 2018
	Description:
		Takes in input from a simple program and scans for certain keyword
		or identifier and crossmatches with the Symbol Table. The Symbol Table is
		essentially a library and whatever you need like a COPY, AND, or OR it checks
		to match those identifiers/keywords.
**/

import java.util.*;
import java.io.*;

public class SimpleAsm
{
	public static final int HALT 	= 0;
	public static final int PUSH 	= 1;
	public static final int RVALUE 	= 2;
	public static final int LVALUE 	= 3;
	public static final int POP 	= 4;
	public static final int STO		= 5;
	public static final int COPY	= 6;
	public static final int ADD		= 7;
	public static final int SUB 	= 8;
	public static final int MPY		= 9;
	public static final int DIV		= 10;
	public static final int MOD		= 11;
	public static final int NEG		= 12;
	public static final int NOT		= 13;
	public static final int OR		= 14;
	public static final int AND		= 15;
	public static final int EQ		= 16;
	public static final int NE		= 17;
	public static final int GT		= 18;
	public static final int GE		= 19;
	public static final int LT		= 20;
	public static final int LE		= 21;
	public static final int LABEL	= 22;
	public static final int GOTO	= 23;
	public static final int GOFALSE	= 24;
	public static final int GOTRUE	= 25;
	public static final int PRINT	= 26;
	public static final int READ	= 27;
	public static final int GOSUB	= 28;
	public static final int RET		= 29;

	public static String [] opcodes = {"HALT","PUSH","RVALUE","LVALUE","POP","STO","COPY","ADD","SUB","MPY","DIV","MOD",
										"NEG","NOT","OR","AND","EQ","NE","GT","GE","LT","LE","LABEL","GOTO","GOFALSE","GOTRUE",
										"PRINT","READ","GOSUB","RET"};

	public static void main(String [] args)throws IOException
	{
		// get filename
		String filename;
		SymbolTable symTab = new SymbolTable();

		if (args.length != 0)
		{
			filename = args[0];
		}
		else
		{
			filename = "simple.asm";
		}

		// Open file for input
		Scanner infile = new Scanner(new File(filename));

		// pass 1 -- build symbol table
		pass1(infile, symTab);
		infile.close();

		// pass 2 -- assemble
		// reopen source file
		infile = new Scanner(new File(filename));

		// pass 2 -- output binary code
		pass2(infile,symTab);
		infile.close();

		// print symbol table
		dumpSymbolTable(symTab);
		System.out.println("Done");
	}

	public static int lookUpOpcode(String s)
	{
		for(int i = 0; i < opcodes.length; i++)
		{
			if (s.equalsIgnoreCase(opcodes[i]))
			{
				return i;
			}
		}
		System.err.println("\nInvalid opcode:" + s);
		return -1;
	}

	public static void pass1(Scanner infile, SymbolTable tab)
	{
		// initialize location counter, etc.
		int locationCounter = 0;
		String line;
		Scanner input;
		String lexeme;

		// find start of data section
		do
		{
			line = infile.nextLine();
			System.out.println(line);
			input = new Scanner(line);
		} while (!input.next().equalsIgnoreCase("Section"));
		if (!input.next().equalsIgnoreCase(".data"))
		{
			System.err.println("Error:  Missing 'Section .data' directive");
			System.exit(1);
		}
		else
		{
			System.out.println("Parsing data section, pass 1");
		}

		// build symbol table from variable declarations
		line = infile.nextLine();
		input = new Scanner(line);

		// data section ends where code section begins
		while(!(lexeme = input.next()).equalsIgnoreCase("Section"))
		{
			// look for labels (they end with a colon)
			int pos = lexeme.indexOf(':');
			if (pos > 0)
			{
				lexeme = lexeme.substring(0,pos);
			}
			else
			{
				System.err.println("error parsing " + line);
			}
			// insert the lexeme, the type, and its address into the symbol table
			tab.insert(lexeme,"Int",locationCounter);
			locationCounter++;
			line = infile.nextLine();
			input = new Scanner(line);
		}

		// Now, parse the code section, looking for the label directive
		System.out.println("Parsing code section, pass 1");
		locationCounter = 0;
		while(infile.hasNext())
		{
			line = infile.nextLine();
			input = new Scanner(line);
			lexeme = input.next();
			// when a label is found, place it and its code offset in the symbol table
			if (lexeme.equalsIgnoreCase("label"))
			{
				lexeme = input.next();
				tab.insert(lexeme,"Code",locationCounter);
			}
			locationCounter++;
		}
	}

	// generate the code
	public static void pass2(Scanner infile, SymbolTable tab)
	{
		// initialize location counter, etc.
		int locationCounter = 0;
		String line;
		Scanner input;
		String lexeme;
		int symTabPtr;
		SymbolTableEntry entry;
		final int NULL = -1;
		// find start of next section
		do
		{
			line = infile.nextLine();
			input = new Scanner(line);

		} while (!input.next().equalsIgnoreCase("Section"));
		if (!input.next().equalsIgnoreCase(".data"))
		{
			System.err.println("Error:  Missing 'Section .data' directive");
			System.exit(1);
		}
		else
		{
			System.out.println("Parsing data section, pass 2");
		}
		line = infile.nextLine();
		input = new Scanner(line);

		while(!(lexeme = input.next()).equalsIgnoreCase("Section"))
		{
			// data section has been processed in previous pass, so skip this
			line = infile.nextLine();
			input = new Scanner(line);
		}

		// Now, let's generate some code
		System.out.println("Parsing code section, pass 2");
		locationCounter=0;
		// while not end of file keep parsing
		while(infile.hasNext())
		{
			line = infile.nextLine();
			input = new Scanner(line);
			//lexeme = input.next();
			int ptr;
			//	lookup opcode and generate appropriate instructions
			int opcode = lookUpOpcode(lexeme);
			switch(opcode)
			{
				case HALT:
					insertCode(locationCounter, HALT);
					break;
				case PUSH:
					lexeme = input.next();
					insertCode(locationCounter, PUSH, Integer.parseInt(lexeme));
					break;
				case RVALUE:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, RVALUE, tab.get(ptr));
					break;
				case LVALUE:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, LVALUE, tab.get(ptr));
					break;
			/**	case ASGN:
					insertCode(locationCounter, ASGN);
					break;	**/
				case POP:
					insertCode(locationCounter, POP);
					break;
				case STO:
					insertCode(locationCounter, STO);
					break;
				case COPY:
					insertCode(locationCounter, COPY);
					break;
				case ADD:
					insertCode(locationCounter, ADD);
					break;
				case SUB:
					insertCode(locationCounter, SUB);
					break;
				case MPY:
					insertCode(locationCounter, MPY);
					break;
				case DIV:
					insertCode(locationCounter, DIV);
					break;
				case MOD:
					insertCode(locationCounter, MOD);
					break;
				case NEG:
					insertCode(locationCounter, NEG);
					break;
				case NOT:
					insertCode(locationCounter, NOT);
					break;
				case OR:
					insertCode(locationCounter, OR);
					break;
				case AND:
					insertCode(locationCounter, AND);
					break;
				case EQ:
					insertCode(locationCounter, EQ);
					break;
				case NE:
					insertCode(locationCounter, NE);
					break;
				case GT:
					insertCode(locationCounter, GT);
					break;
				case GE:
					insertCode(locationCounter, GE);
					break;
				case LT:
					insertCode(locationCounter, LT);
					break;
				case LE:
					lexeme = input.next();
					insertCode(locationCounter, LE);
					break;
				case LABEL:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, LABEL, tab.get(ptr));
					break;
				case GOTO:
					insertCode(locationCounter, GOTO);
					break;
				case GOFALSE:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, GOFALSE, tab.get(ptr));
					break;
				case GOTRUE:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, GOTRUE, tab.get(ptr));
					break;
				case PRINT:
					insertCode(locationCounter, PRINT);
					break;
				case READ:
					insertCode(locationCounter, READ);
					break;
				case GOSUB:
					insertCode(locationCounter, GOSUB);
					break;
				case RET:
					insertCode(locationCounter, RET);
					break;
				default:
					System.err.println("Unimplemented opcode:  " + opcode);
					System.exit(opcode);
			}
			locationCounter++;
		}
	}

	public static void insertCode(int loc, int opcode, int operand)
	{
		System.out.println(loc + ":\t" + opcode + "\t" + operand);;
	}

	public static void insertCode(int loc, int opcode)
	{
		insertCode(loc,opcode,0);
	}

	public static void dumpSymbolTable(SymbolTable tab)
	{
		System.out.println("\nlexeme \ttype \taddress");
		System.out.println("-----------------------------------------");
		for(int i=0; i<tab.size(); i++)
		{
			System.out.println(tab.get(i));
		}
	}
}
public class SymbolTable
	{
		private int locationCont;
		ArrayList<String> lexeme = new ArrayList<String>();
		ArrayList<String> type = new ArrayList<String>();
		ArrayList<Integer> addr = new ArrayList<Integer>();

		public int get(int point)
		{
			return point;
		}

		public void insert(String l, String c, int loc)
		{
			lexeme.add(l);
			type.add(c);
			addr.add(loc);
		}

		public int size()
		{
			locationCont++;
			return locationCont;
		}

		public int lookup(String lex)
		{
			for(int i = 0; i < lex.size(); i++)
			{
				if(lexeme.get(i) == lex)
				{
					return lexeme.get(i);
				}
			}
		}

	}




/**
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

public class SymbolTable
{

    private HashMap st = new HashMap();
    private int locationCont;

    public void insert(String key, Object value) { st.insert(key, value);   }
    public Object get(String key)             { return st.get(key);   }
    public int size(int locationCont)	{return locationCont; }
    //public String toString()                  { return st.toString(); }

    // Return an array contains all of the keys

    public String[] lookup()
    {
        Set keyvalues = st.entrySet();
        String[] lookup = new String[st.size()];
        Iterator it = keyvalues.iterator();
        for (int i = 0; i < st.size(); i++)
        {
            Map.Entry entry = (Map.Entry) it.next();
            lookup[i] = (String) entry.getKey();
        }
        return lookup;
    }
}
**/