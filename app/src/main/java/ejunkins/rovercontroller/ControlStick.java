package ejunkins.rovercontroller;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.AttributeSet;
import android.content.Context;
import android.view.View;

/**
 * Joystick class for sending control signals to the raspberry pi
 */
public class ControlStick extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private float mCenterX;
    private float mCenterY;
    private float mBaseRadius;
    private float mHatRadius;
    public JoystickListener joystickListener;

    public ControlStick(Context context){
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickListener = (JoystickListener) context;
    }

    public ControlStick(Context context, AttributeSet attributes, int style){
        super(context,attributes,style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickListener = (JoystickListener) context;
    }

    public ControlStick(Context context, AttributeSet attributes){
        super(context, attributes);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickListener = (JoystickListener) context;
    }

    /**
     * When the Joystick is first created draws the initial joysticks
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder){
        setupDimensions();
        drawJoystick(mCenterX, mCenterY);
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int forsat, int width, int height){
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
    }
    /**
     * Gets the dimensions of the space the joystick is allocated to
     */
    private void setupDimensions(){
        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;
        mBaseRadius = Math.min(getWidth(), getHeight()) *4/11;
        mHatRadius = Math.min(getWidth(), getHeight()) / 4;
    }

    /**
     * Draws the joysticks at their updated x and y location based on touch data
     * @param newX X location of touch
     * @param newY Y location of touch
     */

    @SuppressLint("NewApi")
    private void drawJoystick(float newX, float newY){
        int alpha = 255;
        int red = 100;
        int blue = 100;
        int green = 100;
        int ratio = 5;
        if(getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            //Used to make 3-D effect on joystick
            float hypotenuse = (float) Math.sqrt(Math.pow(newX- mCenterX,2) + Math.pow(newY -
                    mCenterY,2));
            float sin = (newY - mCenterY)/hypotenuse;
            float cos = (newX - mCenterX)/hypotenuse;

            Paint paint = new Paint();
            int r = Math.min(getWidth(),getHeight());
            //RectF borderRect = new RectF(mCenterX - getHeight() / 6, mCenterY + getWidth() / 3, mCenterX + getHeight() / 6, mCenterY - getWidth() / 3);

            paint.setColor(Color.TRANSPARENT);
            paint.setStyle(Paint.Style.FILL);
            for (int i = 1; i <= 100; i++) {
                int x = i;
                if (i > 50){
                    x = 2*i;
                } else {
                    x = i;
                }
                colors.setARGB(255, 0, 0, x);
                RectF borderRect = new RectF(
                        mCenterX -  r/6 +i, mCenterY + r*4/10, mCenterX + r/6 - i, mCenterY - r*4/10);
                myCanvas.drawRoundRect(borderRect,50,50, colors);
            }
            //colors.setARGB(255,150,150,150);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(15);
            paint.setStyle(Paint.Style.STROKE);
            myCanvas.drawCircle(mCenterX, mCenterY,Math.min(getWidth(),getHeight())*4/9,paint);

            for (int i = 1; i <= 100; i++) {
                if (i == 1){
                    colors.setARGB(255,255,255,255);
                } else {
                    colors.setARGB(50, i, i, i * 2);
                }
                myCanvas.drawOval(mCenterX - r/4, mCenterY + r/4, mCenterX + r/4, mCenterY - r/4, colors);
            }

            for (int i =1; i <= (int) (mBaseRadius /ratio); i++){
                colors.setARGB(255/i,0,0,0);
                myCanvas.drawCircle(newX-cos*hypotenuse* (ratio/ mBaseRadius)*i,
                        newY - sin *hypotenuse * (ratio/ mBaseRadius)* i, i*(mHatRadius * ratio/
                                mBaseRadius),colors);
            }
            colors.setARGB(255,0,0,0);
            myCanvas.drawCircle(newX,newY, mHatRadius + (int) 0.2* mHatRadius,colors );
            int b1 = (int) (mHatRadius /10);
            int b2 = (int) (mHatRadius /2);
            int b3 = (int) (mHatRadius *2/3);

            for(int i =0; i<= (int)(mHatRadius);i++) {

                if (i <= b1){
                    colors.setARGB(255,0,0,52);
                } else if (i > b1 && i <= b2 ){
                    colors.setARGB(255,0,0,52+2*i);
                } else if (i > b2 && i < b3){
                    colors.setARGB(255,0,0,0);
                } else if (i >= b3){
                    colors.setARGB(255,0,0,255) ;
                }
                myCanvas.drawCircle(newX, newY, mHatRadius - (float) i*2/3, colors);
            }
            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }

    /**
     * Gets the location of the touch and converts them to locations that make sense to draw on based
     * on constraints of joystick
     * @param view
     * @param myEvent Touch event
     * @return
     */
    @Override
    public boolean onTouch(View view, MotionEvent myEvent){
        if (view.equals(this)) {
            if (myEvent.getAction() != myEvent.ACTION_UP) {
                int height = Math.min(getWidth(),getHeight())/3;
                float displacement = (float) Math.sqrt(Math.pow(myEvent.getY() - mCenterY, 2));
                if (displacement < height) {
                    drawJoystick(mCenterX, myEvent.getY());
                    joystickListener.onJoystickMoved((myEvent.getX() - mCenterX)/height, (myEvent.getY() -
                            mCenterY)/height, getId());
                }
                else{
                    float ratio = height/displacement;
                    float constrainedX = mCenterX + (myEvent.getX()- mCenterX)*ratio;
                    float constrainedY = mCenterY + (myEvent.getY()- mCenterY)*ratio;
                    drawJoystick(mCenterX,constrainedY);
                    joystickListener.onJoystickMoved((constrainedX- mCenterX)/height, (constrainedY-
                            mCenterY)/height, getId());
                }
            } else {
                drawJoystick(mCenterX, mCenterY);
                joystickListener.onJoystickMoved(0,0,getId());
            }
        }
        return true;
    }

    /**
     * Listens for changes in each joystick based on its' ID
     */
    public interface JoystickListener {
        void onJoystickMoved(float xPercent, float yPercent, int id);
    }
}
