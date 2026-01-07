// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a switch-statement.
 */
public class JSwitchStatement extends JStatement {
    public boolean hasBreak; // To see if there is a break statement or not
    public String breakLabel; // The label to go to if there is a break statement
    public boolean hasDefault; // Variable to keep track on whether or not there is a default
    // Test expression.
    private JExpression condition;

    // List of switch-statement groups.
    private ArrayList<SwitchStatementGroup> stmtGroup;

    /**
     * Constructs an AST node for a switch-statement.
     *
     * @param line      line in which the switch-statement occurs in the source file.
     * @param condition test expression.
     * @param stmtGroup list of statement groups.
     */
    public JSwitchStatement(int line, JExpression condition,
                            ArrayList<SwitchStatementGroup> stmtGroup) {
        super(line);
        this.condition = condition;
        this.stmtGroup = stmtGroup;
    }
    /** Added analyze and codegen for project5
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        // For break statements, push a reference to this switch statement
        JMember.enclosingStatement.push(this);

        // Analyze the condition and make sure it's an int
        condition = (JExpression) condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.INT);

        // Analyze the case expressions and make sure they are integer literals.
        ArrayList<JExpression> switchLabels = new ArrayList<>();
        int index = 0;
        for (SwitchStatementGroup switchStmtGroup : stmtGroup){
            switchLabels = switchStmtGroup.getSwitchLabels();

            for (int i = 0; i < switchLabels.size(); i++){
                // Just get the corresponding JExpreesion first to check if it is null
                JExpression swtch = (JExpression) switchLabels.get(i);
                if (swtch == null){
                    hasDefault = true;
                } else {
                    // If it is not null, analyze the context
                    swtch.analyze(context);
                    switchLabels.set(i, swtch);
                    boolean isIntLiteral = swtch instanceof JLiteralInt;
                    if (!isIntLiteral){
                        JAST.compilationUnit.reportSemanticError(line(),
                                "A switch case expression is not an instance of JLiteralInt.");
                    }
                }
            }
        }

        // Create a new LocalContext with context as the parent, and analyze the statements
        // in each case group in the new context.
        ArrayList<JStatement> blocks = new ArrayList<>();
        LocalContext localCon = new LocalContext(context);
        for (SwitchStatementGroup switchStmtGroup : stmtGroup){
            blocks = switchStmtGroup.getBlocks();

            for (int i = 0; i < blocks.size(); i++){
                JStatement block = (JStatement) blocks.get(i).analyze(localCon);
                blocks.set(i, block);
            }
        }

        // Pop the reference to this switch statement
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // hi is the highest case label value, lo is the lowest, nlabels are the total number of labels
        int hi = 0;
        int lo = 0;
        int nLabels = 0;
        // String variable for the default label
        String deflt = output.createLabel();

        // Two array lists, one for the sorted case integers and one non-sorted
        ArrayList<Integer> cases = new ArrayList<>();
        ArrayList<Integer> sortedCases = new ArrayList<>();

        for (SwitchStatementGroup switchStmtGroup : stmtGroup){
            for (JExpression swtch : switchStmtGroup.getSwitchLabels()){
                // Swtch would be null if it is the default case; we don't want to work with default currently
                // So if it is not the default case, add the case number to the array lists and increment nLabels
                if (swtch != null){
                    cases.add(((JLiteralInt) swtch).toInt());
                    sortedCases.add(((JLiteralInt) swtch).toInt());
                    nLabels++;
                }
            }
        }
        // Sorts the array list in natural order (lowest to highest)
        sortedCases.sort(Comparator.naturalOrder());
        // Lo is the lowest case value and hi is the highest (last index)
        lo = sortedCases.get(0);
        hi = sortedCases.get(sortedCases.size() - 1);

        // Provided code to calculate whether the opcode is TABLESWITCH or LOOKUPSWITCH
        long tableSpaceCost = 5 + hi - lo ;
        long tableTimeCost = 3;
        long lookupSpaceCost = 3 + 2 * nLabels;
        long lookupTimeCost = nLabels;
        int opcode = nLabels > 0 && ( tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost + 3 * lookupTimeCost ) ?
                TABLESWITCH : LOOKUPSWITCH;

        // TreeMap for LOOKUPSWITCH; the integer value is the case, the string value is a created label
        TreeMap<Integer, String> matchLabelPairs = new TreeMap<>();
        // Arraylist for the String labels
        ArrayList<String> labels = new ArrayList<>();

        // Go through the cases array list and add the integer value to the treemap, along with
        // a created label
        for (int i = 0; i < cases.size(); i++){
            String label = output.createLabel();
            labels.add(label);
            int caseValue = cases.get(i);
            matchLabelPairs.put(caseValue, label);
        }

        // Generate the code for the condition
        condition.codegen(output);

        if (hasBreak){
            breakLabel = output.createLabel();
        }

        if (opcode == TABLESWITCH){
            // Parameters: the default jump label, the lo/hi values, and the labels arraylist
            // The labels array list should not include default
            output.addTABLESWITCHInstruction(deflt, lo, hi, labels);

            // Going through the switch statement groups
            for (SwitchStatementGroup swtchStateGroup : stmtGroup){
                int labelSize = swtchStateGroup.getSwitchLabels().size();

                // Going through the labels and the blocks
                for (int i = 0; i < labelSize; i++){
                    // Variables for the current label and block statement
                    JExpression caseExp = (JExpression) swtchStateGroup.getSwitchLabels().get(i);
                    // Get the Array List for the blocks
                    ArrayList<JStatement> blocks = swtchStateGroup.getBlocks();


                    // Excluding default (the current label will be null if default)
                    // We want to add the labels first and then generate the blocks
                    if (caseExp != null){
                        int switchIndex = ((JLiteralInt) caseExp).toInt();
                        String switchValue = matchLabelPairs.get(switchIndex);
                        // Add the label
                        output.addLabel(switchValue);
                        // Generate this label's block of code

                    } else {
                        // For adding default in the proper place
                        output.addLabel(deflt);
                    }
                    // Generate the code for the blocks
                    for (JStatement block : blocks){
                        block.codegen(output);
                    }
                }
            }
        } else if (opcode == LOOKUPSWITCH){
            // Parameters: the default jump label, the total amount of labels (without default)
            // And a TreeMap with the case integers with labels
            output.addLOOKUPSWITCHInstruction(deflt, nLabels, matchLabelPairs);

            for (SwitchStatementGroup swtchStateGroup : stmtGroup){
                int labelSize = swtchStateGroup.getSwitchLabels().size();

                // Going through the labels and the blocks (should be equal labels and blocks?)SS
                for (int i = 0; i < labelSize; i++){
                    // Variables for the current label and block statement
                    JExpression caseExp = (JExpression) swtchStateGroup.getSwitchLabels().get(i);
                    // Get the Array List for the blocks
                    ArrayList<JStatement> blocks = swtchStateGroup.getBlocks();

                    // Excluding default (the current label will be null if default)
                    // We want to add the labels first and then generate the blocks
                    if (caseExp != null){
                        int switchIndex = ((JLiteralInt) caseExp).toInt();
                        String switchValue = matchLabelPairs.get(switchIndex);
                        // Add the label
                        output.addLabel(switchValue);

                    } else{
                        // For adding the default label
                        output.addLabel(deflt);
                    }
                    // Generate the code for the blocks
                    for (JStatement block : blocks){
                        block.codegen(output);
                    }
                }
            }
        }

        // If there is no default, put this label at the end
        if (!hasDefault){
            output.addLabel(deflt);
        }

        // If there is a break, leave the switch
        if (hasBreak){
            output.addLabel(breakLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JSwitchStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        for (SwitchStatementGroup group : stmtGroup) {
            group.toJSON(e);
        }
    }
}

/**
 * A switch statement group consists of case labels and a block of statements.
 */
class SwitchStatementGroup {
    // Case labels.
    private ArrayList<JExpression> switchLabels;

    // Block of statements.
    private ArrayList<JStatement> block;

    /**
     * Constructs a switch-statement group.
     *
     * @param switchLabels case labels.
     * @param block        block of statements.
     */
    public SwitchStatementGroup(ArrayList<JExpression> switchLabels, ArrayList<JStatement> block) {
        this.switchLabels = switchLabels;
        this.block = block;
    }

    /**
     * Added for project5
     * @return the switch labels for this switch statement group
     */
    public ArrayList<JExpression> getSwitchLabels(){
        return switchLabels;
    }

    /**
     * Added for project5
     * @return the block statements for this switch statement group
     */
    public ArrayList<JStatement> getBlocks(){
        return block;
    }

    /**
     * Stores information about this switch statement group in JSON format.
     *
     * @param json the JSON emitter.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("SwitchStatementGroup", e);
        for (JExpression label : switchLabels) {
            JSONElement e1 = new JSONElement();
            if (label != null) {
                e.addChild("Case", e1);
                label.toJSON(e1);
            } else {
                e.addChild("Default", e1);
            }
        }
        if (block != null) {
            for (JStatement stmt : block) {
                stmt.toJSON(e);
            }
        }
    }
}