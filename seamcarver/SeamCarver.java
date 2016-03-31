import java.awt.Color;

public class SeamCarver {
   private Picture picture;
   private double BORDER_ENERGY = 195075;
   private double[][] pictureEnergy;
   private Color[][] colorArray;
   private boolean isTransposed = false;
   private int width;
   private int height;
   
   public SeamCarver(Picture picture){
       //Copy image into a new reference
       this.picture = picture;
       this.width = this.picture.width();
       this.height = this.picture.height();
       
       this.colorArray = new Color[this.height()][this.width()];
       
       //retrieve the color array
       for(int y = 0; y < this.picture.width(); y++){
           for(int x = 0; x < this.picture.height(); x++){
               this.colorArray[x][y] = this.picture.get(y, x);
           }
       }
       
       this.calculateEnergy();
   }
   
   private void calculateEnergy(){
       this.pictureEnergy = new double[this.height()][this.width()];
       
       //calculate energy
       for(int x = 0; x < this.height(); x++){
           for(int y = 0; y < this.width(); y++){
               this.pictureEnergy[x][y] = this.energy(y, x);
           }
       }
   }
   
   // current picture
   public Picture picture(){
       Picture returnPicture = new Picture(this.width(), this.height());
       //compose a new picture from the color grid
       for(int x = 0; x < this.height(); x++){
           for(int y = 0; y < this.width(); y++){
               returnPicture.set(y, x, this.colorArray[x][y]);
           }
       }
       
       this.picture = returnPicture;
       return returnPicture;
   }       
      
   // width of current picture
   public int width(){
       return !this.isTransposed ? this.width : this.height;
   }             
               
   // height of current picture
   public int height(){
       return !this.isTransposed ? this.height : this.width;
   }           
   
   public void setWidth(int w){
       if(!this.isTransposed){
           this.width = w;
       } else {
           this.height = w;
       }
   }
                
   // energy of pixel at column x and row y
   public double energy(int x, int y){
       if (y == 0 || y == this.height() - 1 || x == 0 || x == this.width() - 1 ) {
           return BORDER_ENERGY;
       }
       Color xLeft = this.colorArray[y][x-1];
       Color xRight = this.colorArray[y][x+1];
       Color yUp = this.colorArray[y-1][x];
       Color yDown = this.colorArray[y+1][x];
       
       return ((dualGradientCount(xRight, xLeft) + dualGradientCount(yDown, yUp)));
   }         
   
   private double dualGradientCount(Color rgb1, Color rgb2){
       return this.countPowTwo(rgb1.getRed(), rgb2.getRed()) + 
           this.countPowTwo(rgb1.getGreen(), rgb2.getGreen()) +
           this.countPowTwo(rgb1.getBlue(), rgb2.getBlue());
   }
   
   private int countPowTwo(int x, int y){
       return Math.abs(x - y) * Math.abs(x - y);
   }
   
   // sequence of indices for horizontal seam
   public int[] findHorizontalSeam(){
       this.transpose(false);
       int[] horSeam = this.findVerticalSeam();
       this.transpose(true);
       
       return horSeam;
   }            
   
   // sequence of indices for vertical seam
   // REMEMBER: x is column, y is row!!
   public int[] findVerticalSeam(){
       double[][] distTo = new double[this.height()][this.width()];    //shortest distance from any of top 3 pixels
       int[][] edgeTo = new int[this.height()][this.width()];            //which top 3 pixels the shortest distance comes from, -1 left 0 top 1 right
       
       //initiate distance and edge for every pixel, top is 0 distance, rest is infinity
       for(int y = 0; y < this.width(); y++){
           for(int x = 0; x < this.height(); x++){
               if(x == 0){
                   distTo[x][y] = 0;
               } else {
                   distTo[x][y] = Double.POSITIVE_INFINITY;
               }
               edgeTo[x][y] = 0;   //Set to default "top"
           }
       }
       
       //calculate the distance using topological order, which is the top row items, flash flood until you fill bottom - 1 row
       for(int x = 0; x < this.height() - 1; x++){
           for(int y = 0; y < this.width(); y++){
               //check bottom left pixel
               if(y != 0){
                   if(distTo[x+1][y-1] > distTo[x][y] + this.pictureEnergy[x+1][y-1]){
                       //this is smaller path sets
                       distTo[x+1][y-1] = distTo[x][y] + this.pictureEnergy[x+1][y-1];
                       edgeTo[x+1][y-1] = 1;
                   }
               }
               
               //check bottom pixel
               if(distTo[x+1][y] > distTo[x][y] + this.pictureEnergy[x+1][y]){
                   distTo[x+1][y] = distTo[x][y] + this.pictureEnergy[x+1][y];
                   edgeTo[x+1][y] = 0;
               }
               
               //check bottom right pixel
               if(y < this.width() - 1){
                   if(distTo[x+1][y+1] > distTo[x][y] + this.pictureEnergy[x+1][y+1]){
                       distTo[x+1][y+1] = distTo[x][y] + this.pictureEnergy[x+1][y+1];
                       edgeTo[x+1][y+1] = -1;
                   }
               }
               
           }
       }
       
       //now check bottom - 1 row and see which column item has the min distance from top
       int smallestColumn = 0;
       double smallestDistance = Double.POSITIVE_INFINITY;
       for(int y = 0; y < this.width(); y++){
           if(distTo[this.height()-1][y] < smallestDistance){ 
               smallestColumn = y;
               smallestDistance = distTo[this.height()-1][y];
           }
       }
       
       //traverse up the matrix retrieving the path
       int[] seam = new int[this.height()];
       seam[this.height()-1] = smallestColumn;
       for(int x = this.height() - 1; x > 0; x--){
          seam[x-1] = seam[x] + edgeTo[x][seam[x]]; 
       }
       
       return seam;
   }       
   
   public void transpose(boolean transposeBack){
       this.isTransposed = !transposeBack;
       
       //re-arrange picture energy
       double[][] energyTransposed = new double[this.height()][this.width()];   //width and height already return transposed dimension
       for(int x = 0; x < this.pictureEnergy.length; x++){          //height
           for(int y = 0; y < this.pictureEnergy[0].length; y++){   //width
               energyTransposed[y][x] = this.pictureEnergy[x][y];
           }
       }
       
       //re-arrange color grid
       Color[][] colorTransposed = new Color[this.height()][this.width()];
       //retrieve the color array
       for(int x = 0; x < this.colorArray.length; x++){
           for(int y = 0; y < this.colorArray[0].length; y++){
               colorTransposed[y][x] = this.colorArray[x][y];
           }
       }
       
       //update reference
       this.pictureEnergy = energyTransposed;
       this.colorArray = colorTransposed;
   }
   
   // remove horizontal seam from picture
   public void removeHorizontalSeam(int[] a){
       this.transpose(false);
       this.removeVerticalSeam(a);
       this.transpose(true);
   }
   
   // remove vertical seam from picture
   public void removeVerticalSeam(int[] a){
       //remember x is column y is row
       Color[][] newColor = new Color[this.height()][this.width() - 1];
       
       for(int i = 0; i < a.length; i++){
           //for each row, copy the item in color grid
           System.arraycopy(this.colorArray[i], 0, newColor[i], 0, a[i]);
           System.arraycopy(this.colorArray[i], a[i] + 1, newColor[i], a[i], this.width() - (a[i] + 1));
       }
       this.colorArray = newColor;
       
       //update width count, we decrease it by 1
       this.setWidth(this.width() - 1);
       
       //run the energy function again 
       this.calculateEnergy();
   }     
}