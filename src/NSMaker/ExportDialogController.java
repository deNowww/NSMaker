package NSMaker;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ExportDialogController {
    public GridPane rootGridPane;
    public StackPane previewPane;
    public VBox exportSidebar;
    public ComboBox typeComboBox;
    public Spinner<Integer> marginSizeSpinner;
    public ColorPicker backgroundColorPicker;
    public CheckBox doRemoveTextBackgroundCheckBox;
    public Button cancelBtn;
    public Button okButton;
    
    @FXML
    private void initialize() {
        marginSizeSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        backgroundColorPicker.valueProperty().addListener(((observableValue, color, t1) -> updatePreview()));
        
        updatePreview();
    }
    
    @FXML
    private void updatePreview() {
        Pane content = (Pane) Main.getController().editor.getContent();
        Point2D minXY = minXY(content);
        Group destination = new Group();
        for (Node node : content.getChildren()) {
            Group group = (Group) node;
            flatten(!doRemoveTextBackgroundCheckBox.isSelected(), minXY.add(marginSizeSpinner.getValue(), marginSizeSpinner.getValue()),
                    group, 0, 0, destination);
        }
        // Platform.runLater() is the solution to all problems, and will likely be used to solve world peace.
        Platform.runLater(() -> {
            double scale = Math.min(previewPane.getWidth()/(destination.minWidth(0.0)+marginSizeSpinner.getValue()),
                                    previewPane.getHeight()/(destination.minHeight(0.0)+marginSizeSpinner.getValue()));
            destination.setScaleX(scale);
            destination.setScaleY(scale);
            previewPane.getChildren().clear();
            previewPane.getChildren().add(destination);
            Color bgColor = backgroundColorPicker.getValue();
            previewPane.setBackground(new Background(new BackgroundFill(bgColor, null, null)));
            if (bgColor.getOpacity() > 0) {
                previewPane.getStyleClass().remove("export-preview-transparent-background");
            } else {
                previewPane.getStyleClass().add("export-preview-transparent-background");
            }
        });
        previewPane.setMinWidth(rootGridPane.getPrefWidth() * (rootGridPane.getColumnConstraints().get(1).getPercentWidth()/100));
        previewPane.setMinHeight(rootGridPane.getPrefHeight() * (rootGridPane.getRowConstraints().get(1).getPercentHeight()/100));
    }
    
    private void flatten(boolean doTextBackground, Point2D baseOffset, Group source, double offsetX, double offsetY, Group destination) {
        for (Node child : source.getChildren()) {
            if (child instanceof Group) {
                flatten(doTextBackground, baseOffset, (Group) child, offsetX+child.getLayoutX()+child.getTranslateX(),
                        offsetY+child.getLayoutY()+child.getTranslateY(), destination);
            } else if (child instanceof SVGPath) {
                SVGPath newSVGPath = new SVGPath();
                newSVGPath.setContent(((SVGPath) child).getContent());
                newSVGPath.setStroke(((SVGPath) child).getStroke());
                newSVGPath.setStrokeWidth(((SVGPath) child).getStrokeWidth());
                newSVGPath.setFill(((SVGPath) child).getFill());
                newSVGPath.setLayoutX(child.getLayoutX()+offsetX - baseOffset.getX());
                newSVGPath.setLayoutY(child.getLayoutY()+offsetY - baseOffset.getY());
                destination.getChildren().add(newSVGPath);
            } else if (child instanceof TextField) {
                TextField newTextField = new TextField(((TextField) child).getText());
                newTextField.setPrefWidth(((TextField) child).getPrefWidth());
                newTextField.setLayoutX(child.getLayoutX()+offsetX - baseOffset.getX());
                newTextField.setLayoutY(child.getLayoutY()+offsetY - baseOffset.getY());
                newTextField.setAlignment(((TextField) child).getAlignment());
                newTextField.setStyle("-fx-background-color: " + (doTextBackground ? "#eee" : "transparent") + ";\n" +
                                      "-fx-background-insets: 0;\n" +
                                      "-fx-background-radius: 0;\n" +
                                      "-fx-opacity: 1;");
                newTextField.setDisable(true);
                destination.getChildren().add(newTextField);
            }
            // ignore everything else; there should only be SVGPaths and TextFields
        }
    }
    
    private org.w3c.dom.Document toSVG(Group group, Color backgroundColor) {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        org.w3c.dom.Document doc = impl.createDocument(svgNS, "svg", null);
        
        double width = (group.prefWidth(0.0) + 2*marginSizeSpinner.getValue());
        double height = (group.prefHeight(0.0) + 2*marginSizeSpinner.getValue());
        
        Element svgRoot = doc.getDocumentElement();
        svgRoot.setAttributeNS(null, "width", "" + width);
        svgRoot.setAttributeNS(null, "height", "" + height);
        
        Element fullBGRect = doc.createElementNS(null, "rect");
        fullBGRect.setAttributeNS(null, "fill", "#" + backgroundColor.toString().substring(2, 8));
        fullBGRect.setAttributeNS(null, "fill-opacity", "" + backgroundColor.getOpacity());
        fullBGRect.setAttributeNS(null, "x", "0");
        fullBGRect.setAttributeNS(null, "y", "0");
        fullBGRect.setAttributeNS(null, "width", "" + width);
        fullBGRect.setAttributeNS(null, "height", "" + height);
        svgRoot.appendChild(fullBGRect);
    
        ArrayList<Element> whitespaces = new ArrayList<>();
        ArrayList<Element> nonWhitespaces = new ArrayList<>();
        
        for (Node node : group.getChildren()) {
            Element path = null;
            Element bgRect = null;
            Element text = null;
            if (node instanceof SVGPath) {
                path = doc.createElementNS(null, "path");
//                path.setAttributeNS(null, "x", "" + node.getLayoutX());
//                path.setAttributeNS(null, "y", "" + node.getLayoutY());
                String originalRawSvgPathContent = ((SVGPath) node).getContent();
                Point2D initialMoveToCommand = getInitialMoveToCommand(originalRawSvgPathContent);
                String d = String.format("m%s %s%s", initialMoveToCommand.getX() + node.getLayoutX(), initialMoveToCommand.getY() + node.getLayoutY(),
                        originalRawSvgPathContent.substring(ordinalIndexOf(originalRawSvgPathContent, " ", 2)));
                path.setAttributeNS(null, "d", d);
                path.setAttributeNS(null, "fill", "#" + ((SVGPath) node).getFill().toString().substring(2, 8));
                path.setAttributeNS(null, "stroke", "#" + ((SVGPath) node).getStroke().toString().substring(2, 8));
                path.setAttributeNS(null, "stroke-width", "" + ((SVGPath) node).getStrokeWidth());
//                svgRoot.appendChild(path);
                if (node.getStyleClass().contains("whitespace")) {
                    whitespaces.add(path);
                } else {
                    nonWhitespaces.add(path);
                }
            } else if (node instanceof TextField) {
                double textFieldHeight = 27.0; // hardcoded because i'm tired and this bitch releases in 7 days
                
                if (!doRemoveTextBackgroundCheckBox.isSelected()) {
                    bgRect = doc.createElementNS(null, "rect");
                    bgRect.setAttributeNS(null, "x", "" + node.getLayoutX());
                    bgRect.setAttributeNS(null, "y", "" + node.getLayoutY());
                    bgRect.setAttributeNS(null, "width", "" + ((TextField) node).getPrefWidth());
                    bgRect.setAttributeNS(null, "height", "" + textFieldHeight);
                    bgRect.setAttributeNS(null, "fill", "#eee");
                    nonWhitespaces.add(bgRect);
                }
                
                text = doc.createElementNS(null, "text");
                text.setAttributeNS(null, "x", "" + (node.getLayoutX() + ((TextField) node).getPrefWidth()/2));
                text.setAttributeNS(null, "y", "" + (node.getLayoutY() + textFieldHeight/2 + 5));
                text.setAttributeNS(null, "font-family", "sans-serif");
                text.setAttributeNS(null, "text-anchor", "middle");
//                text.setAttributeNS(null, "dominant-baseline", "middle");
//                text.setAttributeNS(null, "");
                text.setTextContent(((TextField) node).getText());
    
//                svgRoot.appendChild(bgRect);
//                svgRoot.appendChild(text);
                nonWhitespaces.add(text);
            }
//            if (path != null) {
//                svgRoot.appendChild(path);
//            }
//            if (bgRect != null) {
//                svgRoot.appendChild(bgRect);
//            }
//            if (text != null) {
//                svgRoot.appendChild(text);
//            }
        }
        
        for (Element whitespace : whitespaces) {
            svgRoot.appendChild(whitespace);
        }
        for (Element elem : nonWhitespaces) {
            svgRoot.appendChild(elem);
        }
        
        return doc;
    }
    
    private static Point2D getInitialMoveToCommand(String path) {
        String raw = path.substring(1, ordinalIndexOf(path, " ", 2));
        String[] split = raw.split(" ");
        return new Point2D(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
    }
    
    /**
     * shamelessly yoinked from apache commons.
     * what, you think i'm gonna add another dependency just for five fuckin lines?
     */
    private static int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1)
            pos = str.indexOf(substr, pos + 1);
        return pos;
    }
    
    private static Point2D minXY(Pane content) {
        double minX = Document.DOCUMENT_SIZE;
        double minY = Document.DOCUMENT_SIZE;
        if (content.getChildren().size() > 0) {
            for (Node node : ((Group)content.getChildren().get(0)).getChildren()) {
                Group group = (Group) node;
                if (group.getLayoutX() < minX) {
                    minX = group.getLayoutX();
                }
                if (group.getLayoutY() < minY) {
                    minY = group.getLayoutY();
                }
            }
        }
        return new Point2D(minX, minY);
    }
    
    public void export(ActionEvent actionEvent) {
        Pane content = (Pane) Main.getController().editor.getContent();
        Point2D minXY = minXY(content);
        Group destination = new Group();
        for (Node node : content.getChildren()) {
            Group group = (Group) node;
            flatten(!doRemoveTextBackgroundCheckBox.isSelected(), minXY.subtract(marginSizeSpinner.getValue(), marginSizeSpinner.getValue()),
                    group, 0, 0, destination);
//            flatten(!doRemoveTextBackgroundCheckBox.isSelected(), new Point2D(0,0), group, 0, 0, destination);
        }
        org.w3c.dom.Document doc = toSVG(destination, this.backgroundColorPicker.getValue());
        SVGGraphics2D graphics = new SVGGraphics2D(doc);
        FileChooser fc = new FileChooser();
        boolean isPNG = typeComboBox.getValue().equals("PNG");
        if (isPNG) {
//            fc.setInitialFileName("Untitled.png");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG images (*.png)", "*.png"));
        } else {
//            fc.setInitialFileName("Untitled.svg");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG image (*.svg)", "*.svg"));
        }
        
        fc.setTitle("Export");
        File file = fc.showSaveDialog(Main.getController().getExportWindow());
        
        if (isPNG) {
//            try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            try {
                file.createNewFile();
    
                File tmpDir = new File(file.getParent(), ".NSMaker-tmp");
                File tmpFile = new File(tmpDir, "tmp");
                tmpDir.mkdirs();
                tmpFile.createNewFile();
                
                Writer out = new OutputStreamWriter(new FileOutputStream(tmpFile), StandardCharsets.UTF_8);
                graphics.stream(doc.getDocumentElement(), out, true, false);
                
                PNGTranscoder t = new PNGTranscoder();
                TranscoderInput input = new TranscoderInput(tmpFile.toURI().toString());
                OutputStream ostream = new FileOutputStream(file);
                TranscoderOutput output = new TranscoderOutput(ostream);
                t.transcode(input, output);
                ostream.flush();
                ostream.close();
                
                tmpFile.delete();
                tmpDir.delete();
                
                cancelBtn.fire(); // close window
            } catch (IOException | TranscoderException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) { }
        } else {
            try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                file.createNewFile();
                graphics.stream(doc.getDocumentElement(), out, true, false);
                cancelBtn.fire(); // close window
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {
            }
        }
//
//        Writer out;
//        try {
//            out = new OutputStreamWriter(new FileOutputStream("/Users/maxcutlyp/Downloads/test.svg"), StandardCharsets.UTF_8);
//            graphics.stream(out, true);
//            out.flush();
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try {
//            Transformer transformer = TransformerFactory.newInstance().newTransformer();
//            Result output = new StreamResult(new File("/Users/maxcutlyp/Downloads/test.svg"));
//            Source input = new DOMSource(doc);
//            transformer.transform(input, output);
//        } catch (TransformerException e) {
//            e.printStackTrace();
//        }
    
    }
}
