package com.example.christos.embeddedscanner;

import java.util.ArrayList;

public class OptimizedBubblesort {

    public static void sortPricesAndMarkets(ArrayList<Float> prices, ArrayList<String> markets)
    {
        for(int i=0; i<prices.size(); i++)
        {
            boolean flag = false;
            for(int j=0; j<prices.size()-i-1; j++)
            {
                if(prices.get(j)>prices.get(j+1))
                {
                    flag = true;
                    float temp = prices.get(j+1);
                    String tempMar = markets.get(j+1);

                    markets.set(j+1, markets.get(j));
                    markets.set(j, tempMar);

                    prices.set(j+1, prices.get(j));
                    prices.set(j, temp);
                }
            }
            // No Swapping happened, array is sorted
            if(!flag){
                return;
            }
        }
    }
}
