package com.thej.fyp.easycontact;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.widget.Toast;

public class DrawRect extends View implements View.OnClickListener{
    
	private Rect r;  	
	public ArrayList<Rect> faceRecArray = new ArrayList<Rect>();

    public DrawRect(Context context) {
        super(context);
        r = new Rect(0, 0, 1, 1);
        setOnClickListener(this);
    }
    
    public void update(Rect r){
//    	faceRecArray.add(r);
    	this.r = r;
    	invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
    	canvas.drawColor(0x00AAAAAA);
    	Paint myPaint = new Paint();
    	myPaint.setColor(Color.YELLOW);
    	myPaint.setStrokeWidth(10);
    	myPaint.setStyle(Paint.Style.STROKE); 
    	
    	for(int i=0; i<faceRecArray.size(); i++){
        canvas.drawRect(faceRecArray.get(i), myPaint);
//        super.onDraw(canvas);
    	}
    }

	@Override
	public void onClick(View v) {
		System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
		Toast.makeText(getContext(), "heloo!", Toast.LENGTH_SHORT).show();
		
	}

}