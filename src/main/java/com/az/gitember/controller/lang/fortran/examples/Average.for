program average
SOme test
c old comment
* one more comment
! Read in some numbers and take the average
! As written, if there are no data points, an average of zero is returned
! While this may not be desired behavior, it keeps this example simple

  implicit none
  integer :: number_of_points
  real, dimension(:), allocatable :: points
  real :: average_points=0., positive_average=0., negative_average=0.

  write (*,*) "Input number of points to average:"
  read (*,*) number_of_points

  allocate (points(number_of_points))

  write (*,*) "Enter the points to average:"
  read (*,*) points

! Take the average by summing points and dividing by number_of_points
  if (number_of_points > 0) average_points = sum(points)/number_of_points

! Now form average over positive and negative points only
  if (count(points > 0.) > 0) positive_average = sum(points, points > 0.) &
        /count(points > 0.)
  if (count(points < 0.) > 0) negative_average = sum(points, points < 0.) &
        /count(points < 0.)

  deallocate (points)

! Print result to terminal
  write (*,'(''Average = '', 1g12.4)') average_points
  write (*,'(''Average of positive points = '', 1g12.4)') positive_average
  write (*,'(''Average of negative points = '', 1g12.4)') negative_average

end program average