package com.example.Paint;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.io.FileNotFoundException;
import java.io.InputStream;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {
    private PaintView paintView;
    private Button boxbtn;
    private ImageView btnPen, btnEraser, btnPalette, btnUndo, btnLine, btnCircle, btnRectangle, btnPickImage, btnFillColor, btnClear;
    private float previousSliderValue;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private int height, width;

    public MainActivity() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boxbtn = findViewById(R.id.box_btn);
        init();
        defaultValues();
        btnClicks();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            try {
                                Intent data = result.getData();
                                assert data != null;
                                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                bitmap = getResizedBitmap(bitmap, height, width);
                                paintView.setBitmap(bitmap);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        ViewTreeObserver vto = paintView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                paintView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                width = paintView.getMeasuredWidth();
                height = paintView.getMeasuredHeight();
                paintView.init(height, width);// initialize main bitmap with measured width and height
            }
        });
    }


    private void init() {
        paintView = findViewById(R.id.paint_view);
        btnPen = findViewById(R.id.pen);
        btnEraser = findViewById(R.id.eraser);
        btnPalette = findViewById(R.id.color_pick);
        btnUndo = findViewById(R.id.undo);
        btnLine = findViewById(R.id.line);
        btnCircle = findViewById(R.id.circle);
        btnRectangle = findViewById(R.id.square);
        btnPickImage = findViewById(R.id.pick_image);
        btnFillColor = findViewById(R.id.color_fill);
        btnClear = findViewById(R.id.clear_canvas);

    }

    private void defaultValues() {

        paintView.setColor(getResources().getColor(R.color.black));
        paintView.setStrokeWidth(8);
        previousSliderValue = 8;
    }

    private void btnClicks() {

//        pen button
        btnPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Pen active", Toast.LENGTH_SHORT).show();
//                escape erase mode
                paintView.setEraseMode(false);
                // escapse circle mode
                paintView.setCircleMode(false);
                paintView.setColorFillMode(false);
                paintView.setLineMode(false);
                paintView.setRectangleMode(false);

                LayoutInflater layoutInflater = MainActivity.this.getLayoutInflater();
                View layout = layoutInflater.inflate(R.layout.stroke_width_alert, null);

                //AlertDialogBuilder..
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getString(R.string.stroke_width));
                builder.setView(layout);
                Slider sliderStrokeWidth = layout.findViewById(R.id.sliderStrokeWidth);
                sliderStrokeWidth.setValue(previousSliderValue);
                builder.setPositiveButton(getResources().getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        paintView.setStrokeWidth(sliderStrokeWidth.getValue());
                        previousSliderValue = sliderStrokeWidth.getValue();
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


        //EraserButton
        btnEraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Eraser active", Toast.LENGTH_SHORT).show();
                paintView.erase();
                paintView.setCircleMode(false);
                paintView.setLineMode(false);
                paintView.setRectangleMode(false);
                paintView.setColorFillMode(false);
                LayoutInflater layoutInflater = MainActivity.this.getLayoutInflater();
                View layout = layoutInflater.inflate(R.layout.eraser_width_alert, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getString(R.string.eraser_width));
                builder.setView(layout);
                Slider sliderEraserWidth = layout.findViewById(R.id.sliderEraserWidth);
                sliderEraserWidth.setValue(previousSliderValue);

                builder.setPositiveButton(getResources().getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        paintView.setEraserWidth(sliderEraserWidth.getValue());
                        previousSliderValue = sliderEraserWidth.getValue();
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


        //ColorPalette
        btnPalette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(getBaseContext(), "Select Color", Toast.LENGTH_SHORT).show();
                final AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(MainActivity.this, paintView.getColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        paintView.setColor(color);
                    }
                });
                colorPicker.show();
            }
        });
        //Undo Drawing
        btnUndo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
              //  Toast.makeText(getBaseContext(), "Undo", Toast.LENGTH_SHORT).show();
                paintView.Undo();
            }
        });

        //DrawLine Function
        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getBaseContext(), "Line Active", Toast.LENGTH_SHORT).show();
                paintView.setCircleMode(false);
                paintView.setLineMode(true);
                paintView.setEraserMode(false);
                paintView.setColorFillMode(false);
                paintView.setRectangleMode(false);

                //btn property
                btnLine.setSelected(!btnLine.isSelected());
                if (btnLine.isSelected()){
                    btnLine.setBackground(getBaseContext().getResources().getDrawable(R.drawable.btn_unsel_color));
                }else{
                    btnLine.setBackground(getBaseContext().getResources().getDrawable(R.drawable.btn_background_border));
                }

            }
        });

        //DrawCircle function
        btnCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getBaseContext(), "Circle Active", Toast.LENGTH_SHORT).show();
                paintView.setLineMode(true);
                paintView.setCircleMode(true);
                paintView.setEraseMode(false);
                paintView.setColorFillMode(false);
                paintView.setRectangleMode(false);

                //
                btnCircle.setSelected(!btnCircle.isSelected());
                if (btnCircle.isSelected()){
                    btnCircle.setBackground(getBaseContext().getResources().getDrawable(R.drawable.btn_unsel_color));
                }else{
                    btnCircle.setBackground(getBaseContext().getResources().getDrawable(R.drawable.btn_background_border));
                }
            }

        });

        //DrawRectangle
        btnRectangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getBaseContext(), "Rectangle Active", Toast.LENGTH_SHORT).show();
                paintView.setColorFillMode(false);
                paintView.setLineMode(false);
                paintView.setEraseMode(false);
                paintView.setCircleMode(false);
                paintView.setRectangleMode(true);

                //
                btnRectangle.setSelected(!btnRectangle.isSelected());
                if (btnRectangle.isSelected()){
                    btnRectangle.setBackground(getBaseContext().getResources().getDrawable(R.drawable.btn_unsel_color));
                }else{
                    btnRectangle.setBackground(getBaseContext().getResources().getDrawable(R.drawable.btn_background_border));
                }
            }
        });

        // Image Loading function

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getBaseContext(), "Select Image", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        //Colorfill function

        btnFillColor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               // Toast.makeText(getBaseContext(), "Fill Color", Toast.LENGTH_SHORT).show();
                paintView.setColorFillMode(true);
                paintView.setLineMode(false);
                paintView.setEraserMode(false);
                paintView.setCircleMode(false);
                paintView.setRectangleMode(false);

                final AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(MainActivity.this, paintView.getFillColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        paintView.setFillColor(color);
                    }
                });
                colorPicker.show();
            }
        });

        //Clear Canvas Function
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(getBaseContext(), "Clear Canvas", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getString(R.string.clear_message));

                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        paintView.clearCanvas();
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

    }

//    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
//        if (outputStream != null) {
//            try {
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//                outputStream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
// CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
// RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

// RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
}