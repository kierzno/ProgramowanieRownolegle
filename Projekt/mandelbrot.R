library(foreach)
library(parallel)
library(doParallel)

rm(list=ls())

mandelbrot <- function(n, xpos, ypos) {
  x <-  0
  y <-  0
  for(i in 1:n) {
    oldx <- x
    oldy <- y
    x <-  oldx^2 - oldy^2 + xpos
    y <-  2*oldx*oldy + ypos
    if(sqrt(x^2 + y^2) >= 2) {
      return(i)
    }
  }
  return(1)
}

n <- 200
res <- 4096
xmin = -2
xmax = 1
ymin = -1.5
ymax = 1.5

x <- seq(xmin, xmax, length.out=res)
y <- seq(ymin, ymax, length.out=res)
c <- outer(x,y*1i,FUN="+")

cols <- c(
  colorRampPalette(c("#e7f0fa", "#c9e2f6", "#95cbee", "#0099dc", "#4ab04a", "#ffd73e"))(10), 
  colorRampPalette(c("#eec73a", "#e29421", "#e29421", "#f05336","#ce472e"), bias=2)(90), "black")


k <- matrix(0.0, nrow=length(x), ncol=length(y))

system.time(
  for(i in 1:res) {
    for(j in 1:res) {
      k[i,j] <- mandelbrot(n, Re(c[i,j]), Im(c[i,j]))
    }
  }
)

image(x,y,k,col = cols,xlab="Re(c)",ylab="Im(c)")


k <- matrix(0.0, nrow=length(x), ncol=length(y))

numCores <- detectCores(logical = FALSE)
cl <- makeCluster(numCores)
registerDoParallel(cl)

row <- function(i) {
  r <- matrix(0.0, nrow=res, ncol=1)
  for(j in 1:res) {
    r[j,1] <- mandelbrot(n, Re(c[i,j]), Im(c[i,j]))
  }
  return(r)
}

system.time(
  k <- foreach(i=1:res, .combine = cbind) %dopar% {
    ktmp <- row(i)
    ktmp
  }
) 
image(x,y,k, col = cols,xlab="Re(c)",ylab="Im(c)")
