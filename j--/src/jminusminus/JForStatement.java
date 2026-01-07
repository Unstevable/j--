// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a for-statement.
 */
class JForStatement extends JStatement {
    public boolean hasBreak; // To see if there is a break statement or not
    public String breakLabel; // The label to go to if there is a break statement
    public boolean hasContinue; // To see if there is a continue statement or not
    public String continueLabel; // The label to go to if there is a continue statement
    // Initialization.
    private ArrayList<JStatement> init;

    // Test expression
    private JExpression condition;

    // Update.
    private ArrayList<JStatement> update;

    // The body.
    private JStatement body;

    /**
     * Constructs an AST node for a for-statement.
     *
     * @param line      line in which the for-statement occurs in the source file.
     * @param init      the initialization.
     * @param condition the test expression.
     * @param update    the update.
     * @param body      the body.
     */
    public JForStatement(int line, ArrayList<JStatement> init, JExpression condition,
                         ArrayList<JStatement> update, JStatement body) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    /** Added analyze and codegen for project5
     * {@inheritDoc}
     */
    public JForStatement analyze(Context context) {
        // For break statements, push a reference to this for statement
        JMember.enclosingStatement.push(this);

        // Create a new LocalContext with context as the parent.
        LocalContext localCon = new LocalContext(context);

        if (init != null){
            // Analyze the init in the new context.
            for (int i = 0; i < init.size(); i++){
                JStatement initState = (JStatement) init.get(i).analyze(localCon);
                init.set(i, initState);
            }
        }

        if (condition != null){
            // Analyze the condition in the new context and make sure it’s a boolean.
            condition = (JExpression) condition.analyze(localCon);
            condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        }

        if (update != null){
            // Analyze the update in the new context.
            for (int i = 0; i < update.size(); i++){
                JStatement upState = (JStatement) update.get(i).analyze(localCon);
                update.set(i, upState);
            }
        }
        // Analyze the body in the new context.
        body = (JStatement) body.analyze(localCon);

        // Pop the reference to this for statement
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // Create the labels for the loop and the end of the loop
        String forLabel = output.createLabel();
        String endLabel = output.createLabel();

        // Generate the code for the initialization(s)
        // Initializations are optional; generate the code if they're not null
        if (init != null){
            for (int i = 0; i < init.size(); i++){
                init.get(i).codegen(output);
            }
        }
        // Create a label to go back to for the loop
        output.addLabel(forLabel);
        // Generate the code for the condition (optional, so if not null)
        // We branch when the condition is false; call the three argument codegen
        if (condition != null){
            condition.codegen(output, endLabel, false);
        }
        // If there is a break or continue statement, create the labels
        if (hasBreak){
            breakLabel = output.createLabel();
        }
        if (hasContinue){
            continueLabel = output.createLabel();
        }

        // Generate the code for the body
        body.codegen(output);

        // If the conditional has a continue, add at the appropriate place
        if (hasContinue){
            output.addLabel(continueLabel);
        }

        // Generate the code for the update(s), must be AFTER body (also optional)
        if (update != null){
            for (int i = 0; i < update.size(); i++){
                update.get(i).codegen(output);
            }
        }

        // Go to start of the loop
        output.addBranchInstruction(GOTO, forLabel);
        // Create an ending label for the end of the loop
        output.addLabel(endLabel);

        // If has break is true, add the break label at the end of the body
        if (hasBreak){
            output.addLabel(breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JForStatement:" + line, e);
        if (init != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Init", e1);
            for (JStatement stmt : init) {
                stmt.toJSON(e1);
            }
        }
        if (condition != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Condition", e1);
            condition.toJSON(e1);
        }
        if (update != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Update", e1);
            for (JStatement stmt : update) {
                stmt.toJSON(e1);
            }
        }
        if (body != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Body", e1);
            body.toJSON(e1);
        }
    }
}
