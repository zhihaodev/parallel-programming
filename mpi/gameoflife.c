#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "mpi.h"

#define ITERATIONS 64
#define GRID_WIDTH  256
#define DIM  16     // assume a square grid

void updateRow(int *upperRow, int *lowerRow, int *currRow, int *newCurrRow) {
  int i;
  int count = 0;
  for (i = 0; i < DIM; i++) {
    if (i == 0) 
      count = upperRow[DIM - 1] + upperRow[0] + upperRow[1] + currRow[DIM - 1] + currRow[1] + lowerRow[DIM - 1] + lowerRow[0] + lowerRow[1];
    else if (i == DIM - 1)
      count = upperRow[DIM - 2] + upperRow[DIM - 1] + upperRow[0] + currRow[DIM - 2] + currRow[0] + lowerRow[DIM - 2] + lowerRow[DIM - 1] + lowerRow[0];
    else
      count = upperRow[i - 1] + upperRow[i] + upperRow[i + 1] + currRow[i - 1] + currRow[i + 1] + lowerRow[i - 1] + lowerRow[i] + lowerRow[i + 1];

    if ((currRow[i] == 0 && count == 3) || (currRow[i] == 1 && (count == 2 || count == 3)))
      newCurrRow[i] = 1;
    else
      newCurrRow[i] = 0;
  }
}

int main ( int argc, char** argv ) {

  int global_grid[ 256 ] = {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
    1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 };

  // MPI Standard variable
  int num_procs;
  int ID, j;
  int iters = 0;

  // Messaging variables
  MPI_Status stat;
  // TODO add other variables as necessary
  int next;
  int prev;
  int rowNum;
  int lowerRow[DIM];
  int upperRow[DIM];
  int **currGrid;
  int **newGrid;
  int *newFlatGrid;
  int i;

  // MPI Setup
  if ( MPI_Init( &argc, &argv ) != MPI_SUCCESS )
  {
    printf ( "MPI_Init error\n" );
  }

  MPI_Comm_size ( MPI_COMM_WORLD, &num_procs ); // Set the num_procs
  MPI_Comm_rank ( MPI_COMM_WORLD, &ID );

  assert ( DIM % num_procs == 0 );

  // TODO Setup your environment as necessary
  next = (ID + 1) % num_procs;
  prev = ID == 0 ? num_procs - 1 : ID - 1;
  rowNum = DIM / num_procs;

  currGrid = (int**) malloc(sizeof(int*) * rowNum);
  newGrid = (int**) malloc(sizeof(int*) * rowNum);

  for (i = 0; i < rowNum; i++) {
    currGrid[i] = malloc(sizeof(int) * DIM);
    newGrid[i] = malloc(sizeof(int) * DIM);
    for (j = 0; j < DIM; j++) {
      currGrid[i][j] = global_grid[ID * rowNum * DIM + i * DIM + j];
      newGrid[i][j] = currGrid[i][j];
    }
  }

  for ( iters = 0; iters < ITERATIONS; iters++ ) {
    // TODO: Add Code here or a function call to you MPI code
    if (num_procs == 1) {
      updateRow(currGrid[rowNum - 1], currGrid[1], currGrid[0], newGrid[0]);
      updateRow(currGrid[rowNum - 2], currGrid[0], currGrid[rowNum - 1], newGrid[rowNum - 1]);
      for (j = 1; j < rowNum - 1; j++) 
        updateRow(currGrid[j - 1], currGrid[j + 1], currGrid[j], newGrid[j]);

      for (j = 0; j < rowNum; j++)
        memcpy(currGrid[j], newGrid[j], sizeof(int) * DIM);

      for (j = 0; j < DIM * rowNum; j++)
        global_grid[j] = currGrid[j / DIM][j % DIM];
      
    } else {
      if (ID % 2 == 0) {
        MPI_Ssend(currGrid[rowNum - 1], DIM, MPI_INT, next, 1, MPI_COMM_WORLD);
        MPI_Ssend(currGrid[0], DIM, MPI_INT, prev, 2, MPI_COMM_WORLD);
        MPI_Recv(upperRow, DIM, MPI_INT, prev, 3, MPI_COMM_WORLD, &stat);
        MPI_Recv(lowerRow, DIM, MPI_INT, next, 4, MPI_COMM_WORLD, &stat);
      } else {
        MPI_Recv(upperRow, DIM, MPI_INT, prev, 1, MPI_COMM_WORLD, &stat);
        MPI_Recv(lowerRow, DIM, MPI_INT, next, 2, MPI_COMM_WORLD, &stat);
        MPI_Ssend(currGrid[rowNum - 1], DIM, MPI_INT, next, 3, MPI_COMM_WORLD);
        MPI_Ssend(currGrid[0], DIM, MPI_INT, prev, 4, MPI_COMM_WORLD);
      }

      if (rowNum == 1) {
        updateRow(upperRow, lowerRow, currGrid[0], newGrid[0]);
      } else {
        updateRow(upperRow, currGrid[1], currGrid[0], newGrid[0]);
        updateRow(currGrid[rowNum - 2], lowerRow, currGrid[rowNum - 1], newGrid[rowNum - 1]);
        for (j = 1; j < rowNum - 1; j++) 
          updateRow(currGrid[j - 1], currGrid[j + 1], currGrid[j], newGrid[j]);
      }

      for (j = 0; j < rowNum; j++)
        memcpy(currGrid[j], newGrid[j], sizeof(int) * DIM);

      newFlatGrid = (int*) malloc(sizeof(int) * DIM * rowNum);
      for (j = 0; j < DIM * rowNum; j++)
        newFlatGrid[j] = currGrid[j / DIM][j % DIM];

      if (ID != 0) {
        MPI_Ssend(newFlatGrid, DIM * rowNum, MPI_INT, 0, 5, MPI_COMM_WORLD);
      } else {
        for (i = 0; i < num_procs; i++) {
          if (i != 0)
            MPI_Recv(newFlatGrid, DIM * rowNum, MPI_INT, i, 5, MPI_COMM_WORLD, &stat);
          for (j = 0; j < DIM * rowNum; j++) 
            global_grid[i * rowNum * DIM + j] = newFlatGrid[j];
        }
      }
      free(newFlatGrid);
    }

    // Output the updated grid state
    // FIXME: Feel free to print more iterations when you debug but only 
    //  submit with the 64th iteration printing and do not change the 
    //  format of the printed output.
    if ( ID == 0 && iters == ITERATIONS-1) {
      printf ( "\nIteration %d: final grid:\n", iters );
      for ( j = 0; j < GRID_WIDTH; j++ ) {
        if ( j % DIM == 0 ) {
          printf( "\n" );
        }
        printf ( "%d  ", global_grid[j] );
      }
      printf( "\n" );
    }
  }

  // TODO: Clean up memory
  free(currGrid);
  free(newGrid);
  MPI_Finalize(); // finalize so I can exit
}