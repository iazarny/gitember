somr 
#include <stdio.h>
     test
int main() { //line comment
    char * z = "dfdfsdfasfdasf";
    int k = 34;
    /* Add 10 and 20 and store result into register %eax */
    __asm__ ( "movl $10, %eax;"
                "movl $20, %ebx;"
                "addl %ebx, %eax;"
    );

    /* Subtract 20 from 10 and store result into register %eax */
    asm ( "movl $10, %eax;"
                    "movl $20, %ebx;"
                    "subl %ebx, %eax;"
    );

    /* Multiply 10 and 20 and store 
       result into register %eax */
    __asm ( "movl $10, %eax;"
                    "movl $20, %ebx;"
                    "imull %ebx, %eax;"
    );

    return 0 ;
}