# Scale font and line width (dpi) by changing the size! It will always display stretched.
set terminal svg size 400,350 enhanced
#fname 'arial'  fsize 10 butt solid

set style line 1 \
    linecolor rgb '#0060ad' \
    linetype 1 linewidth 2 \
    pointtype 7 pointsize 0.8

set style line 2 \
    linecolor rgb '#ad2020' \
    linetype 1 linewidth 2 \
    pointtype 7 pointsize 0.8

set style line 3 \
    linecolor rgb '#ff9900' \
    linetype 1 linewidth 2 \
    pointtype 7 pointsize 0.8

set style line 4 \
    linecolor rgb '#20ad20' \
    linetype 1 linewidth 2 \
    pointtype 7 pointsize 0.8

	
set datafile separator ","

# Key means label...
set key inside bottom right
set xlabel 'Number of executors'
set ylabel 'Tims (s)'
set title 'Processing time'

set output 'images/exp2_multiseg_narrow_time.svg'
plot  "exp2_multiseg_narrow.csv" using 4:7 title '' with linespoints linestyle 1

set output 'images/exp2_multiseg_narrow_records_throughput.svg'
set ylabel '1K records per second'
set title 'Throughput, 1K records per second'

plot  "exp2_multiseg_narrow.csv" using ($4):($5/$7/1000) title '' with linespoints linestyle 2

set output 'images/exp2_multiseg_narrow_mb_throughput.svg'
set ylabel 'MB/s'
set title 'Throughput, MBs per second'

plot  "exp2_multiseg_narrow.csv" using ($4):($6/$7) title '' with linespoints linestyle 3

stats "exp2_multiseg_narrow.csv" using 4:7 name "a" nooutput
time_single_core = a_max_y
set yrange [0:1.3]

set output 'images/exp2_multiseg_narrow_efficiency.svg'
set ylabel 'Efficiency'
set title 'Efficiency'

plot  "exp2_multiseg_narrow.csv" using ($4):(time_single_core/($7*$4)) title '' with linespoints linestyle 4
