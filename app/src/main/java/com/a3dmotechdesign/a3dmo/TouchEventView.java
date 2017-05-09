package com.a3dmotechdesign.a3dmo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by leecheonji on 15. 8. 4..
 */
public class TouchEventView extends View {
    private boolean touched[] = new boolean[2];
    private int x[] = new int[2];
    private int y[] = new int[2];
    private int drawX = 270, drawY = 270, drawA = 90, drawB = 960;
    private int rCenX = 270, rCenY = 960, lCenX = 270, lCenY = 270;
    private int rMaxX = 450, rMaxY = 1140, lMaxX = 450, lMaxY = 450;
    private int rMinX = 90, rMinY = 780, lMinX = 90, lMinY = 90;

    private int imgWidth, imgHeight;
    private Bitmap imgMove, imgMove2;
    private boolean selectR = false, selectL = false;

    public TouchEventView(Context context){
        super(context);
    }

    public boolean onTouchEvent(MotionEvent event){

        int action = event.getAction() & MotionEvent.ACTION_MASK;
        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK);
        pointerIndex = pointerIndex >> MotionEvent.ACTION_POINTER_ID_SHIFT;
        int tId = event.getPointerId(pointerIndex);


        switch(action) {
            case MotionEvent.ACTION_DOWN: //한 개 포인트에 대한 DOWN을 얻을 때.
            case MotionEvent.ACTION_POINTER_DOWN: //두 개 이상의 포인트에 대 DOWN을 얻을 때.
                touched[tId] = true;
                x[tId] = (int)event.getX(pointerIndex);
                y[tId] = (int)event.getY(pointerIndex);
                checkImageMove(x[tId],y[tId]);
                break;

            case MotionEvent.ACTION_MOVE:
                int pointer_count = event.getPointerCount();
                for (int i = 0; i < pointer_count; i++) {
                    pointerIndex = i;
                    tId = event.getPointerId(pointerIndex);
                    x[tId] = (int) event.getX(pointerIndex);
                    y[tId] = (int) event.getY(pointerIndex);
                    if (selectL && y[tId] < 620) {
                        lScope(x[tId], y[tId]);
                    }
                    if (selectR && y[tId] > 620) {
                        rScope(x[tId], y[tId]);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                touched[tId] = false;
                x[tId] = (int) (event.getX(pointerIndex));
                y[tId] = (int) (event.getY(pointerIndex));

                if(y[tId] < 620 && selectL){
                    drawX=lCenX;
                    drawY=lCenY;
                    selectL=false;

                }
                else if(y[tId] > 620 && selectR) {
                    drawA=Throttle(x[tId]);
                    drawB=rCenY;
                    selectR=false;
                }

                break;
        }

        invalidate();  // 화면 갱신
        return true;
    }


    protected void onDraw(Canvas canvas){

        Paint p= new Paint();
        canvas.drawARGB(00, 00, 00, 00);
        p.setTextSize(40); //글자 크기20 설정
        p.setColor(Color.BLACK); // 글자색설정

        canvas.save();
        canvas.rotate(90);
        canvas.drawText("첫번째터치 :" + x[0] + "\n" + "2번째터치 :" + y[0], 10, -650, p);
        canvas.drawText("첫번째터치 :" + x[1] + "\n" + "2번째터치 :" + y[1], 700, -650, p);
        canvas.drawText("첫번째터치 :" + touched[0] + "\n" + "2번째터치 :" + touched[1], 400, -500, p);


        canvas.restore();
        canvas.save();
        canvas.rotate(0);
        imgWidth = 100;
        imgHeight = 100;

        Bitmap lbm = BitmapFactory.decodeResource(getResources(), R.drawable.move2);
        Bitmap rbm = BitmapFactory.decodeResource(getResources(), R.drawable.move2);

        imgMove = Bitmap.createScaledBitmap(lbm, imgWidth, imgHeight, true);
        //setClickable(true);
        imgMove2 = Bitmap.createScaledBitmap(rbm, imgWidth, imgHeight, true);
        //setClickable(true);

        //p.setAntiAlias(true);
        //canvas.drawCircle(lCenX, lCenY, 250, p);
        //canvas.drawCircle(rCenX, rCenY, 250, p);

        // p.setColor(Color.BLACK);
        //p.setStyle(Paint.Style.STROKE);
        //p.setStrokeWidth(5);

        // canvas.drawCircle(100, 620, 600, p);
        canvas.restore();


        canvas.drawBitmap(imgMove, drawX - (imgMove.getWidth() / 2), drawY - (imgMove.getHeight() / 2), null);

        canvas.drawBitmap(imgMove2, drawA - (imgMove2.getWidth() / 2), drawB - (imgMove2.getHeight() / 2), null);

    }

    public int Throttle(int a){
        if(a<90){
            return 90;
        }
        else if(a>450){
            return 450;
        }
        else
            return a;
    }
    public void rScope(int x, int y) {
        if (rMaxX < x) {
            drawA = rMaxX;
        } else if (rMinX > x) {
            drawA = rMinX;
        } else
            drawA = x;

        if (rMaxY < y) {
            drawB = rMaxY;
        } else if (rMinY > y) {
            drawB = rMinY;
        } else
            drawB = y;

    }
    public void lScope(int x, int y){
        if(lMaxX < x){
            drawX = lMaxX;
        }
        else if(lMinX  > x){
            drawX =lMinX;
        }
        else
            drawX = x;

        if(lMaxY < y){
            drawY = lMaxY;
        }
        else if(lMinY > y){
            drawY =lMinY;
        }
        else
            drawY = y;

    }

    private void checkImageMove(int x, int y ){
        int inWidth =60;
        int inHeight =60;

        if((drawX - inWidth <x)&&(x < drawX + inWidth)){
            if((drawY - inHeight < y) && (y< drawY + inHeight)){
                selectL = true;
            }
        }
        if((drawA - inWidth <x)&&(x < drawA + inWidth)){
            if((drawB - inHeight < y) && (y< drawB + inHeight)){
                selectR = true;
            }
        }

    }

    public boolean getSelectL(){
        return selectL;
    }
    public boolean getSelectR(){
        return selectR;
    }
    public void setSelectL(boolean a){
        selectL = a;
    }
    public void setSelectR(boolean a){
        selectR = a;
    }
    public int getlCenX() {return  lCenX;}
    public int getlCenY() {return  lCenY;}
    public int getrCenY() {return  rCenY;}
    public int getDrawX(){
        return drawX;
    }
    public int getDrawY(){
        return drawY;
    }
    public int getDrawA(){
        return drawA;
    }
    public int getDrawB(){
        return drawB;
    }
}
