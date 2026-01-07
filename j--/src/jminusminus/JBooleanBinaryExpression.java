// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * This abstract base class is the AST node for binary expressions that return booleans.
 */
abstract class JBooleanBinaryExpression extends JBinaryExpression {
    /**
     * Constructs an AST node for a boolean binary expression.
     *
     * @param line     line in which the boolean binary expression occurs in the source file.
     * @param operator the boolean binary operator.
     * @param lhs      lhs operand.
     * @param rhs      rhs operand.
     */

    protected JBooleanBinaryExpression(int line, String operator, JExpression lhs,
                                       JExpression rhs) {
        super(line, operator, lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String falseLabel = output.createLabel();
        String trueLabel = output.createLabel();
        this.codegen(output, falseLabel, false);
        output.addNoArgInstruction(ICONST_1); // true
        output.addBranchInstruction(GOTO, trueLabel);
        output.addLabel(falseLabel);
        output.addNoArgInstruction(ICONST_0); // false
        output.addLabel(trueLabel);
    }
}

/**
 * The AST node for an equality (==) expression.
 */
class JEqualOp extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for an equality expression.
     *
     * @param line line number in which the equality expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */

    public JEqualOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "==", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), rhs.type());
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        if (lhs.type().isReference()) {
            output.addBranchInstruction(onTrue ? IF_ACMPEQ : IF_ACMPNE, targetLabel);
        } else {
            output.addBranchInstruction(onTrue ? IF_ICMPEQ : IF_ICMPNE, targetLabel);
        }
    }
}

/**
 * The AST node for a logical-and (&amp;&amp;) expression.
 */
class JLogicalAndOp extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for a logical-and expression.
     *
     * @param line line in which the logical-and expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JLogicalAndOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "&&", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        rhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        if (onTrue) {
            String falseLabel = output.createLabel();
            lhs.codegen(output, falseLabel, false);
            rhs.codegen(output, targetLabel, true);
            output.addLabel(falseLabel);
        } else {
            lhs.codegen(output, targetLabel, false);
            rhs.codegen(output, targetLabel, false);
        }
    }
}

/**
 * The AST node for a logical-or (||) expression.
 */
class JLogicalOrOp extends JBooleanBinaryExpression {
    /** Added analyze and codegen for project5
     * Constructs an AST node for a logical-or expression.
     *
     * @param line line in which the logical-or expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JLogicalOrOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "||", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // Same as and: both sides need to be a boolean result
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        rhs.type().mustMatchExpected(line(), Type.BOOLEAN);
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        if (onTrue) {
            // For LogicalOr, if the first condition is true, we want to then branch
            // because it is either ONE or the other, no need for both
            // Ex: a > b || c > d, if a > b, the whole thing is already true
            String falseLabel = output.createLabel();
            lhs.codegen(output, targetLabel, true); // Go to target on true for first condition
            rhs.codegen(output, falseLabel, false);
            output.addLabel(falseLabel);
        } else {
            lhs.codegen(output, targetLabel, false);
            rhs.codegen(output, targetLabel, false);
        }
    }
}

/**
 * The AST node for a not-equal-to (!=) expression.
 */
class JNotEqualOp extends JBooleanBinaryExpression {
    /** Added analyze and codegen for project5
     * Constructs an AST node for not-equal-to (!=) expression.
     *
     * @param line line number in which the not-equal-to (!=) expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */

    public JNotEqualOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "!=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), rhs.type());
        type = Type.BOOLEAN;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        if (lhs.type().isReference()) {
            // onTrue in this sense would mean they lhs does NOT equal the rhs
            output.addBranchInstruction(onTrue ? IF_ACMPNE : IF_ACMPEQ, targetLabel);
        } else {
            output.addBranchInstruction(onTrue ? IF_ICMPNE : IF_ICMPEQ, targetLabel);
        }
    }
}
