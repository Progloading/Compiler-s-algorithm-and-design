// filename recdes.c
/****************************************************************************
   Programmer:	James Roberson III
   Course:	Compiler Design
   Date:	02/16/2018
   Description: This program is a recursive descent compiler to
		parse and encode expressions from the following
		context-free grammar:

            EXPR --> EXPR + TERM | EXPR - TERM
			TERM --> TERM * FCTR | TERM / FCTR | FCTR
			FCTR --> id | ( EXPR )

  The expressions will be encoded for a hypothetical stack machine with the
  following instructions:

		LOD  A    -- push the value in A on top of the stack
		ADD       -- add the two values on top of the stack, push result
		SUB       -- subtract     "
		MPY       -- multiply     "
		DIV       -- divide       "
******************************************************************************/

#include <stdio.h>  // standard i/o prototypes
#include <string.h> // string function prototypes
#include <ctype.h>  // char function prototypes
//#include <stdlib.h> // standard library
//#include <conio.h>

//INPUT:   begin answer := alpha + 2 * gamma div (C3P0 - R2D2) end

// constants
#define PLUSSYM         '+'
#define MINUSSYM        '-'
#define MULTSYM         '*'
#define DIVSYM          'd'
#define OPENPAREN       '('
#define CLOSEPAREN      ')'
#define BLANK           ' '
#define NULLSTRING		""
#define MOD				'm'
#define SEMI			';'
#define COLON			':'
#define EQUAL			'='
#define CARROT			'^'
#define END				'end'
#define BEGIN			'begin'
#define MAX				100


typedef int boolean;

// function prototypes
void open_data_file(int, char *[]);
void parse(void);
void expr(void);
void term(void);
void factor(void);
void error(char *);
void scan(void);
void program(void);
void stmt(void);
void stmt_list(void);
void emit(char *opcode);
//void num(void);
void primary(void);
boolean identifier(char *token);
boolean num(char *token);

//necessary global variables
FILE *infile;
char lookahead[7];

void main(int argc, char *argv[])
{
  open_data_file(argc, argv);
  program();
  emit("HALT");
  //parse();
  puts("End of Compilation...\n");
  puts("I sort of wanna be a doctor now. Like...Dr. Suess. YEAH, that's perfect! :-)");
}

void open_data_file(int argc, char *argv[])
{
  /* This function opens the data file containign the expression for this
	execution of the compiler. */
  // local variables


					//Simply opens file
  		infile = NULL;
  		if (argc > 1)
  		{
  		  if ( (infile = fopen(argv[1],"r")) == NULL )
  		  {
			 fprintf(stderr,"Error opening input file:  %s",argv[1]);
  		  }
  		}
  		else
  		{
  		  infile = stdin;
		}

  		  					//1st attempt opening file thru user input
					/*
  						FILE *fp;
						char fnamer[100] = "";		//Storing File Path/Name of Image to Display

						printf("\n\nPlease Enter the name of the particular file you want to view: \n");
						scanf("%s",&fnamer);
						fp = fopen(fnamer,"r");
					        if(fp == NULL)
							{
								printf("\n%s\" File NOT FOUND!",fnamer);
								printf("Is this where is crashes at? ");
								getch();
								exit(1);
								emit("HALT");
							}
							else
							{
								fopen(fnamer, "r");
							}
					*/

							//2nd attempt opening file thru user input
					/*
						FILE *fp;
		    			char name[MAX];


		    			printf("Enter filename: ");
		    			fgets(name, MAX, stdin);

		    				if((fp = fopen(name, "w")) == 0)
		    				    printf("File cannot be opened!");

		    					//return 0;
					*/

} // end open_data_file

void error(char *errstr)
{
  fprintf(stderr,"%s\n", errstr);
} // end error

void scan(void)
{
/* This procedure is a simple lexical analyzer; it produces the next token from
   the input string each time it is called */
   int ch; int i;


   strcpy(lookahead,NULLSTRING);
   while (isspace(ch = getc(stdin))) // Ignore any blanks
	{ /* do nothing */}
   switch (ch) {
	case PLUSSYM:
	case MINUSSYM:
	case MULTSYM:
	case DIVSYM:
	case OPENPAREN:
	case CLOSEPAREN:
	case CARROT:
	case EQUAL:
	case SEMI:
	case COLON:


	{ /* note: all operators are 1 character */
	   lookahead[0] = ch;
	   lookahead[1] = '\0';
	   break;
	}
	default:
	   { /* identifiers are any sequence of non-delimiters */
	   i = 0;
	   do {
		   lookahead[i++] = ch;
		   ch = getc(stdin);
	   } while ( (toupper(ch) >= 'A') &&(toupper(ch) <= 'Z')||(toupper(ch) >= '0' && toupper(ch) <= '9'));
	   lookahead[i] = '\0';
	   ungetc(ch,stdin);
	}
    } // end switch
} /*end scan()*/

void match(char *token)
  /* "Matchmaker, Matchmaker, make me a match ... "  -- Fiddler on the Roof*/
{
	if (strcmp(token,lookahead) == 0)  /* then a match has been made*/
	 scan(); /* get new lookahead */
    else
	 error(strcat(token," expected"));     /* report "TOKEN expected" */
}  /* end match() */



boolean identifier(char *token)
{
  /* checks for a valid identifier -- a sequence of nondelimiters starting
    with a letter  */
   return ( ((token[0] >= 'A') && (token[0] <= 'Z')) ||
			   ((token[0] >= 'a') && (token[0] <= 'z')));
} /* end identifier() */

boolean num(char *token)
{
	return ((token[0] >= '0') && (token[0] <= '9'));
}


void emit(char * opcode)
{
   printf("%s\n",opcode);
} /* end emit() */

void fctr()
 /* This procedure handles the assembly of factors */
{
   char s[20] = NULLSTRING;

   /*First, check for an identifier*/
  	primary();
  	while(lookahead[0] == CARROT)
  	{
		strcpy(s,lookahead);
		match(lookahead);
		primary();
		if(strcmp(s,"^") == 0) emit("POW");
	}
} /* end fctr() */


void term()
 /*This procedure handles the assembly of terms */
{
  char temp[20] = NULLSTRING;

   fctr(); /* A term must begin with a factor */
   /* Now, process any multiplying operator */
   while ( (lookahead[0] == MULTSYM) || (lookahead[0] == DIVSYM || lookahead[0] == MOD) ) {
	strcpy(temp,lookahead);
	match(lookahead);
	fctr();
	if (strcmp(temp,"*") == 0)
	 emit("MPY");
	else if (strcmp(temp,"div") == 0)
	 emit("DIV");
	 else if (strcmp(temp,"mod") == 0)
	 emit ("MOD");
   } // end while

} //  end term()



void expr()
{
 /* This procedure handles the assembly of expressions */

  char temp[20] = NULLSTRING;

   term();  /* An expression must begin with a term */
   /* Now, process any adding operators */
   while ((lookahead[0] == PLUSSYM) || (lookahead[0] == MINUSSYM)) {
	strcpy(temp,lookahead);
	match(lookahead);
	term();
	if(strcmp(temp,"+") == 0)
	 emit("ADD");
	else
	 emit("SUB");
    } // end while
} // end expr()

/*
void parse(void)
{
	scan();
	expr();
} // end parse()
*/

void program(void)
{
	scan();
	printf("");
	match("begin");
	//printf("Works so far...");
	stmt_list();
	//printf("Works so far...");
	match("end");
}
void stmt()
{

	char s[20] = NULLSTRING;
	if (identifier(lookahead))
	{
		strcat(s,"LValue ");
		emit(strcat(s, lookahead));
		match(lookahead);
		match(":");
		match("=");
		expr();
		emit("STO");
		//match("=");
	}
	else
	{
		;
	}
}
void stmt_list()
{
	//printf("Works for the good...\n");
	stmt();
	if(lookahead[0] == SEMI)
	{
		match(";");
		stmt_list();
	}
	else
	{
		;
	}

}
void primary()
{
	char s[20] = NULLSTRING;

	if(identifier(lookahead))
	{
		strcat(s,"Rvalue ");
		emit(strcat(s, lookahead));
		match(lookahead);
	}
	else if (lookahead[0] == OPENPAREN)
	{
		match("(");
		expr();
		match(")");
	}
	else if (num(lookahead))
	{
		strcat(s, "PUSH ");
		emit(strcat(s, lookahead));
		match(lookahead);
	}
	else
	{
		printf("Not it!!!");
	}

}
//I sort of wanna be a doctor now. Like...Dr. Suess :-).

