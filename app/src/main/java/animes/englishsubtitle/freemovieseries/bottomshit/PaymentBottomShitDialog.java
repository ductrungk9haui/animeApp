package animes.englishsubtitle.freemovieseries.bottomshit;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.network.model.config.PaymentConfig;

public class PaymentBottomShitDialog extends BottomSheetDialogFragment {

    public static final String PAYPAL = "paypal";
    public static final String STRIP = "strip";
    public static final String RAZOR_PAY = "razorpay";
    public static final String MOBIAMO_PAY = "mobiamo";
    private DatabaseHelper databaseHelper;
    private OnBottomShitClickListener bottomShitClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_payment_bottom_shit, container,
                false);
        databaseHelper = new DatabaseHelper(getContext());
        PaymentConfig config = databaseHelper.getConfigurationData().getPaymentConfig();
        CardView paypalBt, stripBt, razorpayBt, mobiamoBt;
        paypalBt = view.findViewById(R.id.paypal_btn);
        stripBt = view.findViewById(R.id.stripe_btn);
        razorpayBt = view.findViewById(R.id.razorpay_btn);
        mobiamoBt=view.findViewById(R.id.mobiamobtn);
     /*   Space space = view.findViewById(R.id.space2);
        Space space3 = view.findViewById(R.id.space3);*/

        if (!config.getPaypalEnable()) {
            paypalBt.setVisibility(View.GONE);
           // space.setVisibility(View.GONE);
        }

        if (!config.getStripeEnable()) {
            stripBt.setVisibility(View.GONE);
            //space.setVisibility(View.GONE);
        }
        if (!config.getRazorpayEnable()){
            razorpayBt.setVisibility(View.GONE);
           // space3.setVisibility(View.GONE);
        }

        if (!config.getPaymentwallEnable()){
            mobiamoBt.setVisibility(View.GONE);
            //space3.setVisibility(View.GONE);
        }


        paypalBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomShitClickListener.onBottomShitClick(PAYPAL);
            }
        });

        stripBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomShitClickListener.onBottomShitClick(STRIP);
            }
        });

        razorpayBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomShitClickListener.onBottomShitClick(RAZOR_PAY);
            }
        });

        mobiamoBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomShitClickListener.onBottomShitClick(MOBIAMO_PAY);
            }
        });

        return view;

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            bottomShitClickListener = (OnBottomShitClickListener) context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + " must be implemented");
        }

    }

    public interface OnBottomShitClickListener {
        void onBottomShitClick(String paymentMethodName);
    }

}

