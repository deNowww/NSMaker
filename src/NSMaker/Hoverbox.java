package NSMaker;

import javafx.scene.shape.Rectangle;

/**
 * this is created when a user hovers over a region of different hitboxes for one second.
 * it should not be viewed by the end user and it is not part of any nodelist group.
 * its existence is purely to detect when the user is no longer hovering over all the hitboxes,
 * as trying to coordinate between them all would be too hard.
 * it should be handled entirely by the controller.
 *
 * amendment: having hoverboxes visible to the end user may be better UX, so they have 0.2 opacity.
 */
public class Hoverbox extends Rectangle {
    public Hoverbox(double x, double y, double width, double height) {
        super(x, y, width, height);
        
        this.setOpacity(0.2);
//        this.setPickOnBounds(false);
//        this.setViewOrder(0.5); // behind hitboxes (0.0), in front of nodes (1.0)
        
//        this.setOnMouseExited(event -> this.reattach());
//        this.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
//            this.reattach();
//        });

//        this.setEventDispatcher((event, tail) -> {
//            boolean valid = false;
//            if (event instanceof MouseEvent) {
//                double eventX = ((MouseEvent) event).getX();
//                double eventY = ((MouseEvent) event).getY();
//                Point2D toLocal = this.screenToLocal(eventX, eventY);
//                valid = this.contains(toLocal);
//                System.out.println(String.format("event instanceof mouseevent; event x/y: %s/%s; local: %s; valid: %s", eventX, eventY, toLocal, valid));
//                System.out.println(String.format("test: %s", this.contains(10, 10)));
//            }
//            return valid ? tail.dispatchEvent(event) : null;
//        });
        // should set on mouse exited the bounds, regardless of z-index
    }
    
//    private void reattach() {
//        Main.getController().reattach();
//    }
}
