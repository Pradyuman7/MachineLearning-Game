package com.pd.mlgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    /*first two are for previous result second two are for recent result
     *results[0][x][0][x] 0 at index 0 or 2 is a win
     *results[1][x][1][x] 1 at index 0 or 2 is a tie
     *results[2][x][2][x] 2 at index 0 or 2 is a loss
     *results[x][0][x][0] 0 at index 1 or 3 is for rock
     *results[x][1][x][1] 1 at index 1 or 3 is for scissors
     *results[x][2][x][2] 2 at index 1 or 3 is for paper*/

    private int[][][][][][] results6D=new int[3][3][3][3][3][3];
    private int[][][][] results4D=new int[3][3][3][3];
    private int[][] results2D= new int [3][3];

    private int lastResult=-1;
    private int lastCompMove=-1;

    private int secondResult=-1;
    private int secondCompMove=-1;

    private int status=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void userMove(View v) {

        int userMoveInt;
        Button b=(Button) v;
        String name=b.getText().toString();
        switch (name) {
            case ("Rock"): {
                userMoveInt = 0;
                break;
            }
            case ("Paper"): {
                userMoveInt = 1;
                break;
            }
            default:
                userMoveInt = 2;
        }

        int compMoveInt;
        String compMoveStr;
        int resultStatus;
        compMoveInt=nextCompMove();

        if (compMoveInt==0)
            compMoveStr="rock";

        else if (compMoveInt==1)
            compMoveStr="paper";

        else if (compMoveInt==2)
            compMoveStr="scissors";

        else
            compMoveStr="nothing";

        String text="I drew "+compMoveStr+", ";
        resultStatus=checkResult(compMoveInt,userMoveInt);

        if (resultStatus==0)
            text+="You Win!";

        else if (resultStatus==1)
            text+="It's a Tie.";

        else if (resultStatus==2)
            text+="You Lose!";

        TextView output= findViewById(R.id.output);
        output.setText(text);
        trainModel(resultStatus,compMoveInt);

        float[] percentages=analyzeResults();
        int[] changes=changes(percentages);
        int rockChance=changes[0];
        int paperChance=changes[1];

        text = "Rock: "+String.valueOf(rockChance)+"%";
        ((TextView) findViewById(R.id.rPer)).setText(text);
        text = "Paper: "+String.valueOf(paperChance)+"%";
        ((TextView) findViewById(R.id.pPer)).setText(text);
        text = "Scissors: "+String.valueOf(100-rockChance-paperChance)+"%";
        ((TextView) findViewById(R.id.sPer)).setText(text);

        int top=Math.max(rockChance,Math.max(paperChance,100-rockChance-paperChance));
        text = "Confidence\n"+String.valueOf(Math.round(2*(top-50)))+"%";
        ((TextView) findViewById(R.id.confidence)).setText(text);
    }

    private int nextCompMove() {

        if (lastResult==-1)
            return 1;

        int chanceOneHundred=(int)(Math.random()*99)+1;
        float[] percentages=analyzeResults();
        int[] changes=changes(percentages);
        int rockChance=changes[0];
        int paperChance=changes[1];
        int compMove;

        if (chanceOneHundred<rockChance)
            compMove=0;

        else if (chanceOneHundred<rockChance+paperChance)
            compMove=1;

        else
            compMove=2;

        return compMove;
    }

    private int[] changes(float[] percentages) {

        int totalPercentages = (int) (percentages[0]+percentages[1]+percentages[2]);
        int rockChance=33;
        int paperChance=33;

        if (totalPercentages!=0) {
            rockChance= (int) (100*percentages[0]/totalPercentages);
            paperChance= (int) (100*percentages[1]/totalPercentages);
        }

        return new int[]{rockChance, paperChance};
    }

    private float[] analyzeResults() {

        float[] percentages=new float[3];
        if (lastResult==-1)
            return percentages;

        int[][] relevantResults;
        //noinspection ConstantConditions
        if (lastResult!=-1) {

            int[][] resultsTo2D;
            int[][] results4DTo2D=results4D[lastResult][lastCompMove];
            if (secondResult!=-1) {
                int[][] results6DTo2D=results6D[secondResult][secondCompMove][lastResult][lastCompMove];
                resultsTo2D=totalAmount(results6DTo2D)<totalAmount(results4DTo2D)&&totalAmount(results6DTo2D)<9?results4DTo2D:results6DTo2D;
            }

            else
                resultsTo2D=results4DTo2D;

            relevantResults=totalAmount(resultsTo2D)<totalAmount(results2D)&&totalAmount(resultsTo2D)<1?results2D:resultsTo2D;
        }
        else
            relevantResults=results2D;
        for (int i=0;i<percentages.length;i++) {

//			percentages[i]=1+(relevantResults[2][i]*4+relevantResults[1][i]-relevantResults[0][1]*2);
            percentages[i]=1+(relevantResults[2][i]+relevantResults[0][i==2?0:i+1])*4-(relevantResults[0][i]+relevantResults[2][i==0?2:i-1])*4;
            if (percentages[i]<0)
                percentages[i]=0;
        }
        return percentages;
    }

//	private float[] analyzeResults2()
//	{
//		float[] percentages=new float[3];
//		if (lastResult==-1)
//			return percentages;
//
//		int[][] results4DTo2D=null;
//		int[][] results6DTo2D=null;
//
//		if (lastResult!=-1)
//		{
//			results4DTo2D=results4D[lastResult][lastCompMove];
//			if (totalAmount(results4DTo2D)<9)
//				results4DTo2D=null;
//			if (secondResult!=-1)
//			{
//				results6DTo2D=results6D[secondResult][secondCompMove][lastResult][lastCompMove];
//				if (totalAmount(results6DTo2D)<9)
//					results6DTo2D=null;
//			}
//		}
//		int uses;
//		for (int i=0;i<percentages.length;i++)
//		{
//			percentages[i]=results2D[0][i]*2+results2D[1][i]-results2D[2][i]*2;
//			uses=1;
//			if (results4DTo2D!=null)
//			{
//				percentages[i] += (results4DTo2D[2][i] + results4DTo2D[0][i == 2 ? 0 : i + 1]) * 4 + results4DTo2D[1][i] * 2 - (results4DTo2D[0][i] + results4DTo2D[2][i == 0 ? 2 : i - 1]) * 4;
//				uses+=2;
//			}
//			if (results6DTo2D!=null)
//			{
//				percentages[i] +=(results6DTo2D[2][i] + results6DTo2D[0][i == 2 ? 0 : i + 1]) * 4 + results6DTo2D[1][i] * 2 - (results6DTo2D[0][i] + results6DTo2D[2][i == 0 ? 2 : i - 1]) * 4;
//				uses+=2;
//			}
//			percentages[i]/=uses;
//			if (percentages[i]<0)
//				percentages[i]=0;
//		}
//		return percentages;
//	}

    private int checkResult(int compMoveInt,int userMoveInt) {
        /*Status 0 is a win
         *Status 1 is a tie
         *Status 2 is a loss*/
        int resultStatus;
        if (compMoveInt==0&&userMoveInt==1||compMoveInt==1&&userMoveInt==2||compMoveInt==2&&userMoveInt==0) {
            resultStatus = 0;
            TextView wins= findViewById(R.id.wins);
            int winAmount=Integer.parseInt(wins.getText().toString());
            wins.setText(String.valueOf(winAmount+1));
        }
        else if (compMoveInt==userMoveInt) {
            resultStatus = 1;
            TextView ties= findViewById(R.id.ties);
            int tieAmount=Integer.parseInt(ties.getText().toString());
            ties.setText(String.valueOf(tieAmount+1));
        }
        else {
            resultStatus = 2;
            TextView losses= findViewById(R.id.losses);
            int lossAmount=Integer.parseInt(losses.getText().toString());
            losses.setText(String.valueOf(lossAmount+1));
        }

        return resultStatus;
    }

    private void trainModel(int resultStatus,int compMoveInt) {
        if (lastResult!=-1) {
            results4D[lastResult][lastCompMove][resultStatus][compMoveInt]++;

            if (secondResult!=-1)
                results6D[secondResult][secondCompMove][lastResult][lastCompMove][resultStatus][compMoveInt]++;
            secondResult=lastResult;
            secondCompMove=lastCompMove;
        }

        results2D[resultStatus][compMoveInt]++;
        lastResult=resultStatus;
        lastCompMove=compMoveInt;
    }

    private static int totalAmount(int[][] results) {
        int total=0;

        for (int[] row: results)
            for (int r: row)
                total+=r;

        return total;
    }

    public void clear(View v) {
        results6D=new int[3][3][3][3][3][3];
        results4D=new int[3][3][3][3];
        results2D= new int [3][3];
        lastResult=-1;
        lastCompMove=-1;
        secondResult=-1;
        secondCompMove=-1;

        ((TextView)findViewById(R.id.losses)).setText("0");
        ((TextView)findViewById(R.id.ties)).setText("0");
        ((TextView)findViewById(R.id.wins)).setText("0");

        float[] percentages=analyzeResults();
        int[] changes=changes(percentages);
        int rockChance=changes[0];
        int paperChance=changes[1];

        String text = "Rock: "+String.valueOf(rockChance)+"%";
        ((TextView) findViewById(R.id.rPer)).setText(text);
        text = "Paper: "+String.valueOf(paperChance)+"%";
        ((TextView) findViewById(R.id.pPer)).setText(text);
        text = "Scissors: "+String.valueOf(100-rockChance-paperChance)+"%";
        ((TextView) findViewById(R.id.sPer)).setText(text);

        int top=Math.max(rockChance,Math.max(paperChance,100-rockChance-paperChance));
        text = "Confidence\n"+String.valueOf(Math.round(2*(top-50)))+"%";
        ((TextView) findViewById(R.id.confidence)).setText(text);
    }
}