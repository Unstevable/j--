/*
Author: Steven Carr
Last Edited: February 2nd, 2024
CS451 Compilers
*/
import java.util.ArrayList;

import jminusminus.CLEmitter;

import static jminusminus.CLConstants.*;

/**
 * This class programmatically generates the class file for the following Java application:
 * 
 * <pre>
 * public class IsPrime {
 *     // Entry point.
 *     public static void main(String[] args) {
 *         int n = Integer.parseInt(args[0]);
 *         boolean result = isPrime(n);
 *         if (result) {
 *             System.out.println(n + " is a prime number");
 *         } else {
 *             System.out.println(n + " is not a prime number");
 *         }
 *     }
 *
 *     // Returns true if n is prime, and false otherwise.
 *     private static boolean isPrime(int n) {
 *         if (n < 2) {
 *             return false;
 *         }
 *         for (int i = 2; i <= n / i; i++) {
 *             if (n % i == 0) {
 *                 return false;
 *             }
 *         }
 *         return true;
 *     }
 * }
 * </pre>
 */
public class GenIsPrime {
    public static void main(String[] args) {
        // Create the CLEmitter instance
        CLEmitter e = new CLEmitter(true);

        // Create the ArrayList to store the modifiers
        ArrayList<String> modifiers = new ArrayList<>();

        // public class IsPrime
        modifiers.add("public");
        e.addClass(modifiers, "IsPrime", "java/lang/Object", null, true);

        // public static void main(String[] args)
        modifiers.clear();
        modifiers.add("public");
        modifiers.add("static");
        e.addMethod(modifiers, "main", "([Ljava/lang/String;)V", null, true);

        // int n = Integer.parseInt(args[0])
        e.addNoArgInstruction(ALOAD_0);  // Push args onto the stack
        e.addNoArgInstruction(ICONST_0);  // Push 0 onto the stack
        e.addNoArgInstruction(AALOAD);  // Pop args and 0 from the stack; push args[0] onto the stack
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Integer", "parseInt",
                "(Ljava/lang/String;)I"); // Invoke static method Integer.parseInt
        e.addNoArgInstruction(ISTORE_1); // Store it at offset of n (0 is arg, 1 is n)

        // boolean result = IsPrime(n)
        e.addNoArgInstruction(ILOAD_1); // Load n from stack
        // Invoke static method isPrime, from IsPrime class, with int argument and boolean (Z) return type
        e.addMemberAccessInstruction(INVOKESTATIC, "IsPrime", "isPrime", "(I)Z");
        e.addNoArgInstruction(ISTORE_2); // Store it at offset of result

        // if (result)
        e.addNoArgInstruction(ILOAD_2);
        e.addNoArgInstruction(ICONST_1);
        e.addBranchInstruction(IF_ICMPNE, "Else");

        // --------------If true, System.out.println(n + " is a prime number");--------------

        // Push System.out onto the stack
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // Create a String buffer for string concatenation (ex: sb = new StringBuffer();)
        e.addReferenceInstruction(NEW, "java/lang/StringBuffer");
        e.addNoArgInstruction(DUP);
        // INVOKESPECIAL for Constructors
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");

        // sb.append(n)
        e.addNoArgInstruction(ILOAD_1);  // Load n from stack
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(I)Ljava/lang/StringBuffer;");  // INVOKEVIRTUAL for instance methods; append takes int n as arg

        // sb.append(" is a prime number")
        e.addLDCInstruction(" is a prime number"); // LDC => Load constant (for literals)
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;"); // arg is a String, return type is StringBuffer

        // System.out.println(sb.toString)
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "toString", "()Ljava/lang/String;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream", "println",
                "(Ljava/lang/String;)V");

        e.addNoArgInstruction(RETURN);  // Return from main function
        //------------------------------------------------------------------------

        // -------Else, System.out.println(n + " is not a prime number");-------
        // Similar steps as above; Push System.out, create string buffer to append
        e.addLabel("Else");
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        e.addReferenceInstruction(NEW, "java/lang/StringBuffer");
        e.addNoArgInstruction(DUP);
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");

        // sb.append(n), followed by sb.append(" is not a prime number")
        e.addNoArgInstruction(ILOAD_1);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(I)Ljava/lang/StringBuffer;");
        e.addLDCInstruction(" is not a prime number");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;");

        // System.out.println(sb.toString)
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString",
                "()Ljava/lang/String;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream", "println",
                "(Ljava/lang/String;)V");

        e.addNoArgInstruction(RETURN);  // Return from main function
        //---------------------------------------------------------------------------

        //--------isPrime method--------
        // private static void isPrime(int n)
        modifiers.clear(); // Clear the modifiers
        modifiers.add("private");
        modifiers.add("static");
        e.addMethod(modifiers, "isPrime", "(I)Z", null, true);

        // Pseudocode for conditionals:
        // if n >= 2, goto A: otherwise return false
        e.addNoArgInstruction(ILOAD_0); // Load n (at offset 0 because it is the argument)
        e.addNoArgInstruction(ICONST_2);  // Constant 2
        e.addBranchInstruction(IF_ICMPGE, "A"); // If n => 2, go to 'A'
        e.addNoArgInstruction(ICONST_0); // Push 0 to stack
        e.addNoArgInstruction(IRETURN);  // 0 is false, return 0

        // A: i = 2
        e.addLabel("A");
        e.addNoArgInstruction(ICONST_2);  // Push 2 onto stack
        e.addNoArgInstruction(ISTORE_1);  // Store at offset for i (should be 1)

        // D: if (i > n / i) goto B:
        e.addLabel("D");
        e.addNoArgInstruction(ILOAD_1); // Load i
        e.addNoArgInstruction(ILOAD_0); // Load n
        e.addNoArgInstruction(ILOAD_1); // Load i again for division
        e.addNoArgInstruction(IDIV); // Divide n by i
        e.addBranchInstruction(IF_ICMPGT, "B");

        // if n % i != 0 goto C:
        e.addNoArgInstruction(ILOAD_0); // Load n
        e.addNoArgInstruction(ILOAD_1); // Load i
        e.addNoArgInstruction(IREM);    // n % i
        e.addBranchInstruction(IFNE, "C"); // If not equal, goto C
        e.addNoArgInstruction(ICONST_0);  // Push 0 to stack
        e.addNoArgInstruction(IRETURN); // Return false if n % i == 0

        // C: increment i by 1, then goto D
        e.addLabel("C");
        e.addNoArgInstruction(ILOAD_1); // Load i
        e.addNoArgInstruction(ICONST_1);  // Push 1 to the stack
        e.addNoArgInstruction(IADD);  // Increment i by 1
        e.addNoArgInstruction(ISTORE_1);  // Store at offset of i
        e.addBranchInstruction(GOTO, "D"); // goto D

        // B: return true
        e.addLabel("B");
        e.addNoArgInstruction(ICONST_1); // Push 1 to stack
        e.addNoArgInstruction(IRETURN); // Return 1; i.e return true

        // Write the file
        e.write();
    }
}
