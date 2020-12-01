package com.e.ftool;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CustomMarkerInfoWindowView implements GoogleMap.InfoWindowAdapter {

    ArrayList<Driver> driver;
    private final View markerItemView;
    Context context;

    public CustomMarkerInfoWindowView(Context context,ArrayList<Driver> driver) {
        markerItemView = LayoutInflater.from(context).inflate(R.layout.marker_info, null);
        this.driver = driver;
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        Button selectDriver = markerItemView.findViewById(R.id.select_driver);

        for(int i=0;i<driver.size();i++){

            if(marker.getTag().equals(driver.get(i).getKey())){

                TextView name = markerItemView.findViewById(R.id.name);
                name.setText(driver.get(i).getName());

                LinearLayout iconLayout = markerItemView.findViewById(R.id.services_icons);
                LinearLayout chargeLayout = markerItemView.findViewById(R.id.services_charge);
                iconLayout.removeAllViews();
                chargeLayout.removeAllViews();

                for(int j=0;j<driver.get(i).getServices().size();j++){

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(90,50);
                    params.setMargins(10,0,10,0);

                    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(90,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    params1.setMargins(10,0,10,0);

                    TextView charge = new TextView(context);
                    charge.setGravity(Gravity.CENTER);
                    charge.setLayoutParams(params1);
                    charge.setText(driver.get(i).getServices().get(j).get("charge")+" /Hr");

                    ImageView icon = new ImageView(context);
                    icon.setLayoutParams(params);

                    String title = driver.get(i).getServices().get(j).get("title");

                    if(title.equals("Tractor"))
                        icon.setImageResource(R.mipmap.ic_tractor1);
                    else if(title.equals("JCB"))
                        icon.setImageResource(R.mipmap.ic_jcb_loader);
                    else if(title.equals("Rotavator"))
                        icon.setImageResource(R.mipmap.ic_rotavator);

                    iconLayout.addView(icon);
                    chargeLayout.addView(charge);

                }

                break;
            }

        }


        return markerItemView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
