
# Scaling Troubles

First attempt was to start with an 8.5"x11" canvas and scale down the images.
Turns out that both enlarging and reducing images sizes results in los of 
quality. Specifically for a reduction, there are less pixels to display 
the same image so it gets blocky. The quality gets particularly poor as we 
need to reduce the image by about 90% (assuming 300 ppi)!

The second attempt was to use a square canvas that was was dimensionally 
six times larger then the image height. This allowed enough room to radiate 
and produce a wheel of sixteen frames with no reduction in quality. However, 
the memory requirements are huge.

The next trick was to scale the the wheel by printing to a US Letter paper 
size with no margins(wheel circumference already padded). This approach yields 
a target dpi of about 2840 which is substantial.

# Parameters

    CANVAS_HEIGHT_IN_INCHES : 322.0
    CANVAS_HEIGHT_IN_PIXELS : 24192
    CANVAS_RESOLUTION_PPI   : 75
    CANVAS_WIDTH_IN_INCHES  : 322.0
    CANVAS_WIDTH_IN_PIXELS  : 24192
    FRAME_HEIGHT_IN_INCHES  : 1.3990683229813676
    FRAME_WIDTH_IN_INCHES   : 1.0559006211180133
    IMAGE_HEIGHT_IN_INCHES  : 53.0
    IMAGE_HEIGHT_IN_PIXELS  : 4032
    IMAGE_RESOLUTION_PPI    : 75
    IMAGE_WIDTH_IN_INCHES   : 40.0
    IMAGE_WIDTH_IN_PIXELS   : 3024
    PRINT_HEIGHT_IN_INCHES  : 11.0
    PRINT_TARGET_PPI        : 2841.176470588235
    PRINT_WIDTH_IN_INCHES   : 8.5
    REDUCION_PERCENTAGE     : 0.9736024844720497
    REDUCTION_FACTOR        : 37.88235294117647
