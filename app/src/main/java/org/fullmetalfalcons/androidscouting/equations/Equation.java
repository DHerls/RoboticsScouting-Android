package org.fullmetalfalcons.androidscouting.equations;


/**
 * Used to evaluate equations as written in the config file
 * Replaces keys with their associated value
 *
 * Created by Dan on 1/13/2016.
 */
public class Equation {

    @SuppressWarnings("FieldCanBeLocal")
//    private String equation;
    private String name;
    private String columnValue;

    /**
     * Constructor for Equation class
     *
     * @param line line from config file that contains equation data
     */
    public Equation(String line) throws EquationParseException {
        try{
            //Line should come in the form EQUATION_NAME=Equation
            String[] splitLine = line.split("=");
            name = splitLine[0].substring(splitLine[0].indexOf(" ")).trim();
//            equation = splitLine[1].trim();
        } catch(ArrayIndexOutOfBoundsException e){
            throw new EquationParseException("Config error, equation missing \"=\"");
        }
    }

    /**
     * Takes the name of the equation, puts all words to lowercase, then capitalizes each word
     *
     * @return Properly formatted name of equation
     */
    public String getName() {
        //Break string apart
        String[] nameSplit = name.split(" ");
        StringBuilder b = new StringBuilder();
        //Put them all to lowercase
        for (String s: nameSplit){
            b.append(s.substring(0,1).toUpperCase()).append(s.substring(1).toLowerCase()).append(" ");
        }
        return b.toString();
    }

    public String getColumnValue(){
        if (this.columnValue !=null){
            return this.columnValue;
        }

        columnValue = name.replace("\\","_")
                .replace("/","_")
                .replace(" ","_")
                .toLowerCase()
                .trim();
        columnValue += "_score";


        return this.columnValue;

    }
}
