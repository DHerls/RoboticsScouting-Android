package org.fullmetalfalcons.androidscouting;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class holds information on each individual element including its type, descriptions, arguments, and keys
 * Holds data from the config file
 *
 * For more information about why data is parsed in a certain way, see the config file.
 *
 * Created by Dan on 1/11/2016.
 */
public class Element {

    private final Activity activity;
    private ElementType type;
    private String[] descriptions;
    private String[] keys;
    private String[] arguments;
    private View view = null;

    //The below elements are used to capture the data between arrows <Like this>
    private final String argumentRegex = "<(.*?)>";
    private final Pattern argumentPattern = Pattern.compile(argumentRegex);


    /**
     * Constructor for the Element Class, requires a line from the config file
     *
     * @param line A line read from the config file
     * @throws ElementParseException If there is a problem with the line/ it does not conform to conventions
     */
    public Element(String line, Activity activity) throws ElementParseException {
        this.activity = activity;
        parseString(line);


    }

    /**
     * Breaks the line apart and assigns its parts to different variables.
     *
     * @param line Line to be parsed
     * @throws ElementParseException If the line to be parsed is malformed
     */
    private void parseString(String line) throws ElementParseException {
        //Arguments in the config file should be separated by ";;"
        String[] splitLine = line.split(";;");


        Matcher argumentMatcher = argumentPattern.matcher(splitLine[0]);
        //Checks to see if there is any information <like this> in the first portion of the line
        if (argumentMatcher.find()){
            //Group 0 includes <>, Group 1 just has the information inside <>
            arguments = argumentMatcher.group(1).split(",");
        }

        //Retrieves the ElementType based on the portion of the first section not in <> i.e. LABEL
        type = ElementType.getElement(splitLine[0].replaceAll(argumentRegex,"").trim());
        if (type==null){
            throw new ElementParseException("Element Type not recognized: " + splitLine[0].replaceAll(argumentRegex,""));
        }


        //Labels don't have keys, so they need to be parsed differently
        if (type!=ElementType.SPACE && type!=ElementType.LABEL){
            //Get Descriptions
            String descriptions = splitLine[1];
            String[] descriptionList = descriptions.split(",");
            for (int i = 0; i<descriptionList.length;i++){
                descriptionList[i]=descriptionList[i].trim();
            }
            this.descriptions = descriptionList;

            //Get keys
            String keys = splitLine[2];
            String[] keyList = keys.split(",");
            for (int i = 0; i<keyList.length;i++){
                keyList[i]=keyList[i].trim();
            }
            this.keys = keyList;

            //If LABEL
        } else if (type==ElementType.LABEL){
            descriptions = new String[1];
            descriptions[0] = splitLine[1].trim();
        }



    }

    /**
     *Simple getter
     *
     * @return Type of Element
     */
    public ElementType getType() {
        return type;
    }

    /**
     *
     * @return Array containing arguments
     */
    public String[] getArguments(){
        return arguments;
    }

    /**
     * Simple getter
     *
     * @return Array containing descriptions
     */
    public String[] getDescriptions() {
        return descriptions;
    }

    /**
     * Simple getter
     *
     * @return Array containing keys
     */
    public String[] getKeys() {
        return keys;
    }

    public View getView(){
        if (view==null){
            generateView();
        }

        return view;
    }

    private void generateView(){
        //LinearLayout main = (LinearLayout) activity.findViewById(R.id.mainLinear);

        LinearLayout ll = new LinearLayout(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(0, 10, 0, 10);
        switch(type){


            case SEGMENTED_CONTROL:

                ll.setOrientation(LinearLayout.VERTICAL);

                TextView tv = new TextView(activity);
                tv.setText(descriptions[0]);
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                ll.addView(tv);

                MultiStateToggleButton mstb = new MultiStateToggleButton(activity);
                mstb.setElements(arguments);
                mstb.setLayoutParams(lp);
                mstb.setButtonState(mstb.getChildAt(0), true);

                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = activity.getTheme();
                theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
                int color = typedValue.data;

                mstb.setColors(color, Color.WHITE);



                ll.addView(mstb);

                view = ll;
                break;
            case TEXTFIELD:
                ll.setOrientation(LinearLayout.VERTICAL);

                tv = new TextView(activity);
                tv.setText(descriptions[0]);
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                ll.addView(tv);

                EditText et = new EditText(activity);
                switch(arguments[0]){
                    case "normal":
                        et.setInputType(InputType.TYPE_CLASS_TEXT);
                        et.setHint("Text");
                        break;
                    case "number":
                        et.setInputType(InputType.TYPE_CLASS_NUMBER);
                        et.setHint("Number");
                        break;
                    case "decimal":
                        et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        et.setHint("Decimal");
                        break;
                    default:
                        et.setInputType(InputType.TYPE_CLASS_TEXT);
                        et.setHint("Text");
                        break;

                }
                et.setLayoutParams(lp);

                ll.addView(et);

                view = ll;

                break;
            case STEPPER:
                ll.setOrientation(LinearLayout.VERTICAL);

                tv = new TextView(activity);
                tv.setText(descriptions[0]);
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                ll.addView(tv);

                NumberPicker np = new NumberPicker(activity);
                np.scrollBy(1,1);
                np.setMinValue(Integer.parseInt(arguments[0]));
                np.setMaxValue(Integer.parseInt(arguments[1]));
                np.setLayoutParams(lp);

                ll.addView(np);

                view = ll;
                break;
            case LABEL:
                tv = new TextView(activity);
                tv.setText(descriptions[0]);
                switch (arguments[0]){
                    case "bold":
                        tv.setTypeface(null, Typeface.BOLD);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        break;
                    case "normal":
                        tv.setTypeface(null, Typeface.NORMAL);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        break;
                    case "distinguished":
                        tv.setTypeface(null, Typeface.BOLD);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                        break;
                    default:
                        tv.setTypeface(null, Typeface.NORMAL);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        break;
                }

                tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

                switch (arguments[1]){
                    case "left":
                        tv.setGravity(Gravity.START);
                        break;
                    case "right":
                        tv.setGravity(Gravity.END);
                        break;
                    case "center":
                        tv.setGravity(Gravity.CENTER);
                        break;
                    default:
                        tv.setGravity(Gravity.START);
                        break;
                }

                view = tv;
                break;
            case SWITCH:
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ll.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

                LinearLayout vLayout;

                for (String d: descriptions){
                    vLayout = new LinearLayout(activity);
                    vLayout.setOrientation(LinearLayout.VERTICAL);

                    tv = new TextView(activity);
                    tv.setText(d);
                    tv.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

                    vLayout.addView(tv);

                    Switch sw = new Switch(activity);
                    sw.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

                    vLayout.addView(sw);

                    LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                    vlp.weight = 1;
                    vLayout.setLayoutParams(vlp);


                    ll.addView(vLayout);
                }

                view = ll;
                break;
            case SLIDER:
                ll.setOrientation(LinearLayout.VERTICAL);

                tv = new TextView(activity);
                tv.setText(descriptions[0]);
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                ll.addView(tv);

                SeekBarWithValues sb = new SeekBarWithValues(activity,Integer.parseInt(arguments[0]),Integer.parseInt(arguments[1]));
                //sb.setMax(Integer.parseInt(arguments[1]));

                ll.addView(sb);

                view = ll;

                break;
            case SPACE:
                Space space = new Space(activity);
                space.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 10));
                view = space;
                break;
        }
    }

    public Object getViewData(){
        LinearLayout ll;
        switch(type){

            case SEGMENTED_CONTROL:
                ll = (LinearLayout) view;
                MultiStateToggleButton mstb = (MultiStateToggleButton) ll.getChildAt(1);
                return mstb.getValue();
            case TEXTFIELD:
                ll = (LinearLayout) view;
                EditText et = (EditText) ll.getChildAt(1);
                return et.getText().toString();
            case STEPPER:
                ll = (LinearLayout) view;
                NumberPicker np = (NumberPicker) ((LinearLayout) view).getChildAt(1);
                return np.getValue();
            case LABEL:
                return "";
            case SWITCH:
                ll = (LinearLayout) view;
                ArrayList<Boolean> bool = new ArrayList<>();
                LinearLayout innerLayout;
                for ( int i = 0; i< ll.getChildCount(); i++){
                   innerLayout = (LinearLayout) ll.getChildAt(i);
                    bool.add(((Switch) innerLayout.getChildAt(1)).isChecked());
                }
                return bool;
            case SPACE:
                return "";
            case SLIDER:
                ll = (LinearLayout) view;
                SeekBarWithValues sbwv = (SeekBarWithValues) ll.getChildAt(1);
                return sbwv.getSeekBar().getProgress();
        }

        return null;
    }

    public void setViewData(Object viewData) {
        LinearLayout ll;
        switch(type){

            case SEGMENTED_CONTROL:
                ll = (LinearLayout) view;
                MultiStateToggleButton mstb = (MultiStateToggleButton) ll.getChildAt(1);
                mstb.setValue((int) viewData);
                break;
            case TEXTFIELD:
                ll = (LinearLayout) view;
                EditText et = (EditText) ll.getChildAt(1);
                et.setText((String) viewData);
                break;
            case STEPPER:
                ll = (LinearLayout) view;
                NumberPicker np = (NumberPicker) ((LinearLayout) view).getChildAt(1);
                np.setValue((int) viewData);
                break;
            case LABEL:
                break;
            case SWITCH:
                ll = (LinearLayout) view;
                ArrayList<Boolean> bool = (ArrayList<Boolean>) viewData;
                LinearLayout innerLayout;
                for ( int i = 0; i< ll.getChildCount(); i++){
                    innerLayout = (LinearLayout) ll.getChildAt(i);
                    ((Switch) innerLayout.getChildAt(1)).setChecked(bool.get(i));
                }
                break;
            case SPACE:
                break;
            case SLIDER:
                ll = (LinearLayout) view;
                SeekBarWithValues sbwv = (SeekBarWithValues) ll.getChildAt(1);
                sbwv.getSeekBar().setProgress((int) viewData);
                break;
        }

    }

    public HashMap<String, Object> getHash(){
        HashMap<String, Object> map = new HashMap<>();
        LinearLayout ll;
        switch(type){

            case SEGMENTED_CONTROL:
                ll = (LinearLayout) view;
                MultiStateToggleButton mstb = (MultiStateToggleButton) ll.getChildAt(1);
                map.put(keys[0], arguments[mstb.getValue()]);
            break;
            case TEXTFIELD:
                ll = (LinearLayout) view;
                EditText et = (EditText) ll.getChildAt(1);
                map.put(keys[0],et.getText().toString());
            break;
            case STEPPER:
                ll = (LinearLayout) view;
                NumberPicker np = (NumberPicker) ((LinearLayout) view).getChildAt(1);
                map.put(keys[0],np.getValue());
            break;
            case LABEL:
                break;
            case SWITCH:
                ll = (LinearLayout) view;
                ArrayList<Boolean> bool = new ArrayList<>();
                LinearLayout innerLayout;
                for ( int i = 0; i< ll.getChildCount(); i++){
                    innerLayout = (LinearLayout) ll.getChildAt(i);
                    map.put(keys[i],((Switch) innerLayout.getChildAt(1)).isChecked());
                }
                break;
            case SPACE:
                break;
            case SLIDER:
                ll = (LinearLayout) view;
                SeekBarWithValues sbwv = (SeekBarWithValues) ll.getChildAt(1);
                map.put(keys[0],sbwv.getSeekBar().getProgress());
            break;
        }

        return map;
    }
}
