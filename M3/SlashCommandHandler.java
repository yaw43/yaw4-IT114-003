package M3;

/*
Challenge 2: Simple Slash Command Handler
-----------------------------------------
- Accept user input as slash commands
  - "/greet <name>" → Prints "Hello, <name>!"
  - "/roll <num>d<sides>" → Roll <num> dice with <sides> and returns a single outcome as "Rolled <num>d<sides> and got <result>!"
  - "/echo <message>" → Prints the message back
  - "/quit" → Exits the program
- Commands are case-insensitive
- Print an error for unrecognized commands
- Print errors for invalid command formats (when applicable)
- Capture 3 variations of each command except "/quit"
*/

import java.util.Scanner;

public class SlashCommandHandler extends BaseClass {
    private static String ucid = "yaw4"; // <-- change to your UCID

    public static void main(String[] args) {
        printHeader(ucid, 2, "Objective: Implement a simple slash command parser.");

        Scanner scanner = new Scanner(System.in);

        // yaw4 10/15/25
        // Can define any variables needed here

        while (true) {
            System.out.print("Enter command: ");
            // get entered text
            String text = scanner.nextLine(); // get input
            if(text.startsWith("/")) // make sure its a valid command
            {
                String[] txtparts = text.split(" "); // split command 
                int totalNum = 0;
                switch(txtparts[0]) // run commands
                {
                    case "/greet":
                        System.out.println("Hello, " + txtparts[1] + "!");
                        break;
                    case "/quit":
                        System.out.println("Quit command. Quitting program.");
                        break;
                    case "/roll":
                        String[] diceNum = txtparts[1].split("d"); // get rid of d and get dice num
                        // System.out.println("dicenum1: " + diceNum[0] + " dicenum2: " + diceNum[1]); // debug
                        for(int i = 0; i < Integer.valueOf(diceNum[0]); i++)
                        {
                           totalNum = (int)(Math.random() * (Integer.valueOf(diceNum[1]) - 1 + 1)) + 1;
                        }
                        System.out.println("Rolled " + diceNum[0] + "d" + diceNum[1] + " and got:" + totalNum); //output
                        break;
                    case "/echo":
                        System.out.println(txtparts[1]);
                        break;
                    default:
                        System.out.println("Invalid command. Please try typing the command again!");
                }
            }
            else
            {
                System.out.println("Invalid Format. Please Try Again!");
            }
            // check if greet
            //// process greet

            // check if roll
            //// process roll
            //// handle invalid formats

            // check if echo
            //// process echo

            // check if quit
            //// process quit

            // handle invalid commnads

            // delete this condition/block, it's just here so the sample runs without edits
            if (1 == 1) {
                System.out.println("Breaking loop");
                break;
            }
        }

        printFooter(ucid, 2);
        scanner.close();
    }
}
