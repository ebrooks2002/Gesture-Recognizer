//Ethan Brooks 2/21/2022
package comp128.gestureRecognizer;

import edu.macalester.graphics.*;
import edu.macalester.graphics.Point;
import edu.macalester.graphics.ui.Button;
import edu.macalester.graphics.ui.TextField;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * The window and user interface for drawing gestures and automatically recognizing them
 * Created by bjackson on 10/29/2016.
 */

public class GestureApp {

    private CanvasWindow canvas;
    private Recognizer recognizer;
    private IOManager ioManager;
    private GraphicsGroup uiGroup;
    private Button addTemplateButton;
    private TextField templateNameField;
    private GraphicsText matchLabel;
    private Deque<Point> path;
    private GraphicsGroup drawingLayer;
    private Drawer drawer;
    private static Point prevPoint;

    public GestureApp(){
        canvas = new CanvasWindow("Gesture Recognizer", 600, 600);
        drawingLayer = new GraphicsGroup();
        recognizer = new Recognizer();
        path = new ArrayDeque<>();
        ioManager = new IOManager();
        drawer = new Drawer();
        setupUI();
    }

    /**
     * Create the user interface
     */
    private void setupUI(){
        matchLabel = new GraphicsText("Match: ");
        matchLabel.setFont(FontStyle.PLAIN, 24);
        canvas.add(matchLabel, 10, 30);

        uiGroup = new GraphicsGroup();
        canvas.add(drawingLayer);

        templateNameField = new TextField();

        addTemplateButton = new Button("Add Template");
        

        Point center = canvas.getCenter();
        double fieldWidthWithMargin = templateNameField.getSize().getX() + 5;
        double totalWidth = fieldWidthWithMargin + addTemplateButton.getSize().getX();


        uiGroup.add(templateNameField, center.getX() - totalWidth/2.0, 0);
        uiGroup.add(addTemplateButton, templateNameField.getPosition().getX() + fieldWidthWithMargin, 0);
        canvas.add(uiGroup, 0, canvas.getHeight() - uiGroup.getHeight());

        Consumer<Character> handleKeyCommand = ch -> keyTyped(ch);
        canvas.onCharacterTyped(handleKeyCommand);

        mouseEvents();
    }

    /**
     * Clears the canvas, but preserves all the UI objects
     */
    private void removallNonUIGraphcisObjects() {
        drawingLayer.removeAll();
        canvas.add(matchLabel);
    }

    /**
     * Handles all mouse events, including for buttons and graphicsgroup.
     */
    private void mouseEvents() {
        canvas.onMouseDown(event -> { removallNonUIGraphcisObjects();
            path.clear();
             drawer.apply(event.getPosition(), event.getPosition(), drawingLayer, path);
            });
        canvas.onDrag(event -> drawer.apply(event.getPosition(), prevPoint, drawingLayer, path));
        canvas.onMouseUp(event -> findMatch());
        addTemplateButton.onClick( () -> addTemplate() );

    }
    /**
     * If there is already a template in template list, this method matches the most similar template to gesture and displays it.
     */
    private void findMatch() { 
        if (recognizer.getTemplatesSize() != 0) {
            Templatematch match = recognizer.recognize(recognizer.doSteps(path));
            matchLabel.setText("Match: " + match.getName() + " " + match.getScore());
        }
    }
    /**
     * Handle what happens when the add template button is pressed. This method adds the points stored in path as a template
     * with the name from the templateNameField textbox. If no text has been entered then the template is named with "no name gesture"
     */
    private void addTemplate() {
        String name = templateNameField.getText();
        if (name.isEmpty()){
            name = "no name gesture";
        }
        recognizer.addTemplate(name, path); 
    }

    /**
     * Handles keyboard commands used to save and load gestures for debugging and to write tests.
     * Note, once you type in the templateNameField, you need to call canvas.requestFocus() in order to get
     * keyboard events. This is best done in the mouseDown callback on the canvas.
     */
    public void keyTyped(Character ch) {
        if (ch.equals('L')){
            String name = templateNameField.getText();
            if (name.isEmpty()){
                name = "gesture";
            }
            Deque<Point> points = ioManager.loadGesture(name+".xml");
            if (points != null){
                recognizer.addTemplate(name, points);
                System.out.println("Loaded "+name);
            }
        }
        else if (ch.equals('s')){
            String name = templateNameField.getText();
            if (name.isEmpty()){
                name = "gesture";
            }
            ioManager.saveGesture(path, name, name+".xml");
            System.out.println("Saved "+name);
        }
    }
    public static void main(String[] args){
        GestureApp window = new GestureApp();
    }

    public static Point getPrevPoint() {
        return prevPoint;
    }

    public static void setPrevPoint(Point point) {
        prevPoint = point;
    }

}
