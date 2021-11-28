# java -jar md2021.jar -t1
# java -jar md2021.jar -t2 "Test Text zum Argument testen"
# java -jar md2021.jar -t3
# java -jar md2021.jar -i test.in
# java -jar md2021.jar -r test.out
# java -jar md2021.jar -i test.in -r test.out

for i in `ls input_files`; do
	echo "---"
	java -jar md2021.jar -i input_files/$i -r result_files/$i.out
	./check.sh  input_files/$i result_files/$i.out
done
